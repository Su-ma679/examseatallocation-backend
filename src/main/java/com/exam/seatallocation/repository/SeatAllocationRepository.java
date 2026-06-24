package com.exam.seatallocation.repository;

import com.exam.seatallocation.model.Exam;
import com.exam.seatallocation.model.SeatAllocation;
import com.exam.seatallocation.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation, Long> {
    List<SeatAllocation> findByExam(Exam exam);
    void deleteByExam(Exam exam);

    @Modifying
    @Query("DELETE FROM SeatAllocation s WHERE s.student.id = :studentId")
    void deleteByStudentId(Long studentId);
}