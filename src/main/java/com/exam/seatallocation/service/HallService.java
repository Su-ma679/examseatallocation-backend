package com.exam.seatallocation.service;

import com.exam.seatallocation.model.Hall;
import com.exam.seatallocation.repository.HallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HallService {
    @Autowired private HallRepository hallRepository;

    public List<Hall> getAllHalls() { return hallRepository.findAll(); }

    public Hall addHall(Hall hall) {
        hall.setTotalSeats(hall.getRows() * hall.getColumns());
        return hallRepository.save(hall);
    }

    public void deleteHall(Long id) { hallRepository.deleteById(id); }
}