package com.exam.seatallocation.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String subjectCode;

    private String subject;
    private String examDate;
    private String startTime;
    private String endTime;
    private String department;
    private int year;
}