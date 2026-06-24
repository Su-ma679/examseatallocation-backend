package com.exam.seatallocation.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "halls")
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String hallName;
    private int totalSeats;

    @Column(name = "num_rows")
    private int rows;

    @Column(name = "num_columns")
    private int columns;
}