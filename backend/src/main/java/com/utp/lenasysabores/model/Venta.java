package com.utp.lenasysabores.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor 
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_nombre", length = 100, nullable = false)
    private String usuarioNombre;

    @Column(name = "usuario_correo", length = 150, nullable = false)
    private String usuarioCorreo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private Double total;

    @Column(length = 50, nullable = false)
    private String estado;

    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    @Column(name = "estado_pago", length = 30)
    private String estadoPago;

    @Column(name = "codigo_transaccion", length = 60)
    private String codigoTransaccion;

    @Column(name = "tarjeta_ultimos_digitos", length = 4)
    private String tarjetaUltimosDigitos;

    @Column(name = "metodo_entrega", length = 30)
    private String metodoEntrega;

    @Column(name = "direccion_entrega", length = 200)
    private String direccionEntrega;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaItem> items = new ArrayList<>();

    @PrePersist
    public void antesDeGuardar() {
        this.fechaHora = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = "PENDIENTE";
        }
    }
}
