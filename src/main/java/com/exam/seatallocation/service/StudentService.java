package com.exam.seatallocation.service;

import com.exam.seatallocation.model.Student;
import com.exam.seatallocation.repository.SeatAllocationRepository;
import com.exam.seatallocation.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StudentService {
    @Autowired private StudentRepository studentRepository;
    @Autowired private SeatAllocationRepository seatAllocationRepository;

    public List<Student> getAllStudents() { return studentRepository.findAll(); }

    public Student addStudent(Student student) { return studentRepository.save(student); }

    @Transactional
    public void deleteStudent(Long id) {
        seatAllocationRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
    }
}