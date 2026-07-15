package com.utp.lenasysabores.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PagoRequest {

    private String metodoEntrega;
    private String direccionEntrega;
    private String metodoPago;
    private String numeroTarjeta;
    private String vencimiento;
    private String cvv;
    private String nombreTarjeta;
    private String numeroOperacion;
    private String numeroYapePlin;
}
