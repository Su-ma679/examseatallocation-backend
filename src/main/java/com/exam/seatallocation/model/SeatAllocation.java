package com.exam.seatallocation.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "seat_allocations")
public class SeatAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Student student;

    @ManyToOne
    private Exam exam;

    @ManyToOne
    private Hall hall;

    private int seatRow;
    private int seatColumn;
}