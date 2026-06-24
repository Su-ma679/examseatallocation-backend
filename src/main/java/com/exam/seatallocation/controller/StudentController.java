package com.exam.seatallocation.controller;

import com.exam.seatallocation.model.Student;
import com.exam.seatallocation.service.ExamService;
import com.exam.seatallocation.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    @Autowired private StudentService studentService;
    @Autowired private ExamService examService;

    @GetMapping("/all")
    public List<Student> getAllStudents() { return studentService.getAllStudents(); }

    @PostMapping("/add")
    public ResponseEntity<?> addStudent(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.addStudent(student));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteStudent(@PathVariable Long id) { studentService.deleteStudent(id); }

    @GetMapping("/canAdd/{department}/{year}")
    public ResponseEntity<?> canAddStudent(
            @PathVariable String department,
            @PathVariable int year) {
        boolean ongoing = examService.isExamOngoing(department, year);
        return ResponseEntity.ok(Map.of("canAdd", !ongoing,
                "message", ongoing ? "Exam is ongoing! Cannot add students." : "OK"));
    }

}