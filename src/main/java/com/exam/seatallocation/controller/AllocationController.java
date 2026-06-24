package com.exam.seatallocation.controller;

import com.exam.seatallocation.model.SeatAllocation;
import com.exam.seatallocation.repository.SeatAllocationRepository;
import com.exam.seatallocation.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocation")
@CrossOrigin(origins = "*")
public class AllocationController {
    @Autowired private AllocationService allocationService;
    @Autowired private SeatAllocationRepository seatAllocationRepository;

    @PostMapping("/generate/{examId}")
    public Map<String, Object> generateAllocation(@PathVariable Long examId) {
        return allocationService.allocateSeats(examId);
    }

    @GetMapping("/{examId}")
    public List<SeatAllocation> getAllocation(@PathVariable Long examId) {
        return allocationService.getAllocationsByExam(examId);
    }

    @GetMapping("/student/{usn}")
    public List<SeatAllocation> getStudentAllocations(@PathVariable String usn) {
        return allocationService.getAllAllocationsByUSN(usn);
    }

    @GetMapping("/all")
    public List<SeatAllocation> getAllAllocations() {
        return seatAllocationRepository.findAll();
    }
}