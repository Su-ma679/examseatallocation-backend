package com.exam.seatallocation.service;

import com.exam.seatallocation.model.*;
import com.exam.seatallocation.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class AllocationService {

    @Autowired private StudentRepository studentRepository;
    @Autowired private HallRepository hallRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private SeatAllocationRepository seatAllocationRepository;

    @Transactional
    public Map<String, Object> allocateSeats(Long examId) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        Map<String, Object> response = new HashMap<>();

        // Filter students for this exam
        List<Student> allStudents = studentRepository.findAll();
        List<Student> students = new ArrayList<>();
        for (Student s : allStudents) {
            if (exam.getDepartment().equals("ALL")) {
                students.add(s);
            } else if (s.getDepartment().equals(exam.getDepartment())
                    && s.getYear() == exam.getYear()) {
                students.add(s);
            }
        }

        if (students.isEmpty()) {
            response.put("status", "error");
            response.put("message", "No students found for this exam!");
            response.put("allocations", new ArrayList<>());
            return response;
        }

        Collections.shuffle(students);

        // Get halls already occupied during this exam's time slot
        Set<Long> occupiedHallIds = getOccupiedHallIds(exam);

        // Get only FREE halls
        List<Hall> allHalls = hallRepository.findAll();
        List<Hall> freeHalls = new ArrayList<>();
        for (Hall h : allHalls) {
            if (!occupiedHallIds.contains(h.getId())) {
                freeHalls.add(h);
            }
        }

        if (freeHalls.isEmpty()) {
            response.put("status", "warning");
            response.put("message", "⚠️ No free halls available during this exam time slot! All halls are occupied by other exams.");
            response.put("allocations", new ArrayList<>());
            return response;
        }

        // Sort free halls by capacity ascending (dynamic selection)
        freeHalls.sort(Comparator.comparingInt(h -> h.getRows() * h.getColumns()));

        // Select minimum halls needed
        int totalNeeded = students.size();
        int totalCapacity = 0;
        List<Hall> selectedHalls = new ArrayList<>();

        for (Hall hall : freeHalls) {
            if (totalCapacity >= totalNeeded) break;
            selectedHalls.add(hall);
            totalCapacity += hall.getRows() * hall.getColumns();
        }

        if (totalCapacity < totalNeeded) {
            response.put("status", "warning");
            response.put("message", "⚠️ Not enough free seats! Need " + totalNeeded +
                    " seats but only " + totalCapacity + " available in free halls. " );
            response.put("allocations", new ArrayList<>());
            return response;
        }

        // Clear previous allocations for this exam
        seatAllocationRepository.deleteByExam(exam);
        seatAllocationRepository.flush();

        List<SeatAllocation> allocations = new ArrayList<>();
        List<Student> remaining = new ArrayList<>(students);
        boolean isSingleDept = !exam.getDepartment().equals("ALL");

        for (Hall hall : selectedHalls) {
            if (remaining.isEmpty()) break;

            int rows = hall.getRows();
            int cols = hall.getColumns();

            if (isSingleDept) {
                // CHECKERBOARD: empty seats between same-dept students
                for (int r = 0; r < rows && !remaining.isEmpty(); r++) {
                    for (int c = 0; c < cols && !remaining.isEmpty(); c++) {
                        if ((r + c) % 2 == 0) {
                            SeatAllocation allocation = new SeatAllocation();
                            allocation.setStudent(remaining.remove(0));
                            allocation.setExam(exam);
                            allocation.setHall(hall);
                            allocation.setSeatRow(r);
                            allocation.setSeatColumn(c);
                            allocations.add(allocation);
                        }
                    }
                }
            } else {
                // GRAPH COLORING + BACKTRACKING
                List<int[]> seats = new ArrayList<>();
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++)
                        seats.add(new int[]{r, c});

                int studentsToPlace = Math.min(remaining.size(), seats.size());
                Map<String, String> colorMap = new HashMap<>();
                List<Student> placed = new ArrayList<>();
                boolean[] used = new boolean[remaining.size()];

                backtrack(seats, 0, studentsToPlace, remaining, used, placed, colorMap);

                for (int i = 0; i < placed.size(); i++) {
                    int[] seat = seats.get(i);
                    SeatAllocation allocation = new SeatAllocation();
                    allocation.setStudent(placed.get(i));
                    allocation.setExam(exam);
                    allocation.setHall(hall);
                    allocation.setSeatRow(seat[0]);
                    allocation.setSeatColumn(seat[1]);
                    allocations.add(allocation);
                    colorMap.put(seat[0] + "-" + seat[1], placed.get(i).getDepartment());
                }
                remaining.removeAll(placed);
            }
        }

        List<SeatAllocation> saved = seatAllocationRepository.saveAll(allocations);
        response.put("status", "success");
        response.put("message", "✅ " + saved.size() + " students allocated in " +
                selectedHalls.size() + " hall(s): " +
                selectedHalls.stream().map(Hall::getHallName)
                        .reduce((a, b) -> a + ", " + b).orElse("") +
                " using " + (isSingleDept ? "Checkerboard Pattern" : "Graph Coloring + Backtracking") + "!");
        response.put("allocations", saved);
        return response;
    }

    // Get hall IDs already used by other exams at overlapping times
    private Set<Long> getOccupiedHallIds(Exam currentExam) {
        Set<Long> occupiedIds = new HashSet<>();
        try {
            List<Exam> allExams = examRepository.findAll();
            LocalDate currentDate = LocalDate.parse(currentExam.getExamDate());
            LocalTime currentStart = LocalTime.parse(currentExam.getStartTime());
            LocalTime currentEnd = LocalTime.parse(currentExam.getEndTime());

            for (Exam other : allExams) {
                // Skip current exam itself
                if (other.getId().equals(currentExam.getId())) continue;

                LocalDate otherDate = LocalDate.parse(other.getExamDate());
                if (!currentDate.equals(otherDate)) continue;

                LocalTime otherStart = LocalTime.parse(other.getStartTime());
                LocalTime otherEnd = LocalTime.parse(other.getEndTime());

                // Check time overlap
                boolean overlaps = currentStart.isBefore(otherEnd)
                        && currentEnd.isAfter(otherStart);

                if (overlaps) {
                    // Get halls used by this overlapping exam
                    List<SeatAllocation> otherAllocations =
                            seatAllocationRepository.findByExam(other);
                    for (SeatAllocation sa : otherAllocations) {
                        occupiedIds.add(sa.getHall().getId());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking occupied halls: " + e.getMessage());
        }
        return occupiedIds;
    }

    private boolean backtrack(List<int[]> seats, int idx, int total,
                              List<Student> students, boolean[] used,
                              List<Student> placed, Map<String, String> colorMap) {

        if (placed.size() == total) return true;
        if (idx >= seats.size()) return false;

        int[] seat = seats.get(idx);
        int r = seat[0], c = seat[1];

        Set<String> neighborDepts = new HashSet<>();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            String key = (r+d[0]) + "-" + (c+d[1]);
            if (colorMap.containsKey(key))
                neighborDepts.add(colorMap.get(key));
        }

        for (int i = 0; i < students.size(); i++) {
            if (used[i]) continue;
            Student s = students.get(i);
            if (!neighborDepts.contains(s.getDepartment())) {
                used[i] = true;
                placed.add(s);
                colorMap.put(r + "-" + c, s.getDepartment());

                if (backtrack(seats, idx+1, total, students, used, placed, colorMap))
                    return true;

                used[i] = false;
                placed.remove(placed.size()-1);
                colorMap.remove(r + "-" + c);
            }
        }
        return backtrack(seats, idx+1, total, students, used, placed, colorMap);
    }

    public List<SeatAllocation> getAllocationsByExam(Long examId) {
        Exam exam = examRepository.findById(examId).orElseThrow();
        return seatAllocationRepository.findByExam(exam);
    }

    public List<SeatAllocation> getAllAllocationsByUSN(String usn) {
        List<SeatAllocation> all = seatAllocationRepository.findAll();
        List<SeatAllocation> result = new ArrayList<>();
        for (SeatAllocation a : all)
            if (a.getStudent().getRollNo().equalsIgnoreCase(usn.trim()))
                result.add(a);
        return result;
    }
}