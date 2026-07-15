package com.utp.lenasysabores.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.utp.lenasysabores.model.Producto;
import com.utp.lenasysabores.model.Promocion;
import com.utp.lenasysabores.repository.ProductoRepository;
import com.utp.lenasysabores.repository.PromocionRepository;

@Service
public class CatalogoService {

    private final ProductoRepository productoRepository;
    private final PromocionRepository promocionRepository;

    public CatalogoService(
            ProductoRepository productoRepository,
            PromocionRepository promocionRepository) {

        this.productoRepository = productoRepository;
        this.promocionRepository = promocionRepository;
    }

    public List<Producto> obtenerProductos() {
        return productoRepository.findAll();
    }

    public List<Promocion> obtenerPromociones() {
        return promocionRepository.findAll();
    }
    public void guardarProducto(Producto producto) {
    productoRepository.save(producto);
}
public void eliminarProducto(Integer id) {
    productoRepository.deleteById(id);
}
    // Buscar un producto por su ID
public Producto obtenerProductoPorId(Integer id) {
    return productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con el ID: " + id));
}
}
