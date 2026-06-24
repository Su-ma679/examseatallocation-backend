package com.exam.seatallocation.controller;

import com.exam.seatallocation.model.Hall;
import com.exam.seatallocation.service.HallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/halls")
@CrossOrigin(origins = "*")
public class HallController {
    @Autowired private HallService hallService;

    @GetMapping("/all")
    public List<Hall> getAllHalls() { return hallService.getAllHalls(); }

    @PostMapping("/add")
    public Hall addHall(@RequestBody Hall hall) { return hallService.addHall(hall); }

    @DeleteMapping("/delete/{id}")
    public void deleteHall(@PathVariable Long id) { hallService.deleteHall(id); }
}