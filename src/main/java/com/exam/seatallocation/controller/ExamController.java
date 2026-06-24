package com.exam.seatallocation.controller;

import com.exam.seatallocation.model.Exam;
import com.exam.seatallocation.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "*")
public class ExamController {
    @Autowired private ExamService examService;

    @GetMapping("/all")
    public List<Exam> getAllExams() { return examService.getAllExams(); }

    @PostMapping("/add")
    public Map<String, Object> addExam(@RequestBody Exam exam) {
        return examService.addExam(exam);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteExam(@PathVariable Long id) { examService.deleteExam(id); }

    @GetMapping("/ongoing/{department}/{year}")
    public Map<String, Object> checkOngoing(
            @PathVariable String department,
            @PathVariable int year) {
        boolean ongoing = examService.isExamOngoing(department, year);
        return Map.of("ongoing", ongoing,
                "message", ongoing ?
                        "An exam is currently ongoing for your department!" : "OK");
    }
}