package com.utp.lenasysabores.repository;

import com.utp.lenasysabores.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // Buscar todas las ventas de un usuario específico
    List<Venta> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}