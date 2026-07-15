package com.utp.lenasysabores.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.utp.lenasysabores.model.Reserva;
import com.utp.lenasysabores.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public void registrar(Reserva reserva) {
        reservaRepository.save(reserva);
    }

    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }
    public void eliminar(Integer id) {
    reservaRepository.deleteById(id);
}
}