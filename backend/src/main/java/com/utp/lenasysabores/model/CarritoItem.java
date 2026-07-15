package com.utp.lenasysabores.model;

import java.io.Serializable;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor 
public class CarritoItem implements Serializable {

    private Integer productoId;
    private String nombre;
    private double precio;
    private String imagenUrl;
    private int cantidad;

    public CarritoItem(Integer productoId, String nombre, double precio, String imagenUrl) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
        this.cantidad = 1; 
    }

   
    public double getSubtotal() {
        return precio * cantidad;
    }
}