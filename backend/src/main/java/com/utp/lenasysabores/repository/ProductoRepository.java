package com.utp.lenasysabores.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.utp.lenasysabores.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}
