package com.exam.seatallocation.service;

import com.exam.seatallocation.model.Exam;
import com.exam.seatallocation.repository.ExamRepository;
import com.exam.seatallocation.repository.SeatAllocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ExamService {
    @Autowired private ExamRepository examRepository;
    @Autowired private SeatAllocationRepository seatAllocationRepository;

    public List<Exam> getAllExams() { return examRepository.findAll(); }

    public Map<String, Object> addExam(Exam exam) {
        Map<String, Object> response = new HashMap<>();

        // Check subject code uniqueness
        List<Exam> all = examRepository.findAll();
        boolean dupCode = all.stream()
                .anyMatch(e -> e.getSubjectCode().equalsIgnoreCase(exam.getSubjectCode()));
        if (dupCode) {
            response.put("status", "error");
            response.put("message", "Subject code already exists!");
            return response;
        }

        // Check time conflict — same dept/year students cannot have overlapping exams
        for (Exam existing : all) {
            boolean sameDate = existing.getExamDate().equals(exam.getExamDate());
            boolean deptOverlap = existing.getDepartment().equals("ALL")
                    || exam.getDepartment().equals("ALL")
                    || existing.getDepartment().equals(exam.getDepartment());
            boolean yearMatch = existing.getYear() == exam.getYear();

            if (sameDate && deptOverlap && yearMatch) {
                LocalTime existStart = LocalTime.parse(existing.getStartTime());
                LocalTime existEnd = LocalTime.parse(existing.getEndTime());
                LocalTime newStart = LocalTime.parse(exam.getStartTime());
                LocalTime newEnd = LocalTime.parse(exam.getEndTime());

                // Check overlap
                boolean overlaps = newStart.isBefore(existEnd) && newEnd.isAfter(existStart);
                if (overlaps) {
                    response.put("status", "error");
                    response.put("message", "⚠️ Time conflict! Students from " +
                            exam.getDepartment() + " Year " + exam.getYear() +
                            " already have exam '" + existing.getSubject() +
                            "' (" + existing.getStartTime() + " - " + existing.getEndTime() +
                            ") on " + exam.getExamDate() + "!");
                    return response;
                }
            }
        }

        Exam saved = examRepository.save(exam);
        response.put("status", "success");
        response.put("message", "Exam scheduled!");
        response.put("exam", saved);
        return response;
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id).orElseThrow();
        seatAllocationRepository.deleteByExam(exam);
        examRepository.deleteById(id);
    }

    public boolean isExamOngoing(String department, int year) {
        try {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            for (Exam exam : examRepository.findAll()) {
                LocalDate examDate = LocalDate.parse(exam.getExamDate());
                if (!today.equals(examDate)) continue;
                LocalTime start = LocalTime.parse(exam.getStartTime());
                LocalTime end = LocalTime.parse(exam.getEndTime());
                boolean deptMatch = exam.getDepartment().equals("ALL")
                        || exam.getDepartment().equals(department);
                boolean yearMatch = exam.getYear() == year;
                if (now.isAfter(start) && now.isBefore(end) && deptMatch && yearMatch)
                    return true;
            }
        } catch (Exception e) { return false; }
        return false;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoDeleteExpiredExams() {
        try {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            for (Exam exam : examRepository.findAll()) {
                LocalDate examDate = LocalDate.parse(exam.getExamDate());
                LocalTime endTime = LocalTime.parse(exam.getEndTime());
                if (examDate.isBefore(today) ||
                        (examDate.equals(today) && now.isAfter(endTime))) {
                    seatAllocationRepository.deleteByExam(exam);
                    examRepository.delete(exam);
                }
            }
        } catch (Exception e) {
            System.out.println("Auto delete error: " + e.getMessage());
        }
    }
}