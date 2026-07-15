package com.utp.lenasysabores.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PagoResultado {

    private String metodoPago;
    private String estadoPago;
    private String codigoTransaccion;
    private String tarjetaUltimosDigitos;
    private String metodoEntrega;
    private String direccionEntrega;
}
