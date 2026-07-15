package com.utp.lenasysabores.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor 
@Table(name = "promocion")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", length = 100, nullable = false)
    private String titulo;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio_antes", nullable = false)
    private Double precioAntes;

    @Column(name = "precio_ahora", nullable = false)
    private Double precioAhora;

    @Column(name = "etiqueta", length = 50)
    private String etiqueta;

    @Column(name = "imagen_url", length = 300)
    private String imagenUrl;


    public Promocion(Integer id, String titulo, String descripcion,
                     Double precioAntes, Double precioAhora,
                     String etiqueta, String imagenUrl) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precioAntes = precioAntes;
        this.precioAhora = precioAhora;
        this.etiqueta = etiqueta;
        this.imagenUrl = imagenUrl;
    }

    
}
