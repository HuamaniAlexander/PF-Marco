package com.utp.lenasysabores.service;

import com.utp.lenasysabores.model.PagoRequest;
import com.utp.lenasysabores.model.PagoResultado;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

@Service
public class PagoService {

    public PagoResultado procesarPago(PagoRequest pagoRequest, double total) {
        if (total <= 0) {
            throw new IllegalArgumentException("El total del pedido debe ser mayor a cero");
        }

        validarEntrega(pagoRequest);
        String metodoPago = normalizar(pagoRequest.getMetodoPago());

        if ("TARJETA".equals(metodoPago)) {
            validarTarjeta(pagoRequest);
            return new PagoResultado(
                    metodoPago,
                    "PAGADO",
                    generarCodigoTransaccion("CARD"),
                    obtenerUltimosDigitos(pagoRequest.getNumeroTarjeta()),
                    normalizar(pagoRequest.getMetodoEntrega()),
                    limpiar(pagoRequest.getDireccionEntrega())
            );
        }

        if ("YAPE_PLIN".equals(metodoPago)) {
            validarOperacionDigital(pagoRequest);
            return new PagoResultado(
                    metodoPago,
                    "PENDIENTE",
                    "YP-" + limpiar(pagoRequest.getNumeroOperacion()),
                    null,
                    normalizar(pagoRequest.getMetodoEntrega()),
                    limpiar(pagoRequest.getDireccionEntrega())
            );
        }

        throw new IllegalArgumentException("Selecciona un metodo de pago valido");
    }

    private void validarEntrega(PagoRequest pagoRequest) {
        String metodoEntrega = normalizar(pagoRequest.getMetodoEntrega());
        if (!"DELIVERY".equals(metodoEntrega) && !"RECOJO".equals(metodoEntrega)) {
            throw new IllegalArgumentException("Selecciona delivery o recojo en local");
        }

        if ("DELIVERY".equals(metodoEntrega) && limpiar(pagoRequest.getDireccionEntrega()).length() < 8) {
            throw new IllegalArgumentException("Ingresa una direccion de entrega valida");
        }
    }

    private void validarTarjeta(PagoRequest pagoRequest) {
        String numeroTarjeta = limpiar(pagoRequest.getNumeroTarjeta()).replaceAll("\\s+", "");
        String cvv = limpiar(pagoRequest.getCvv());
        String nombreTarjeta = limpiar(pagoRequest.getNombreTarjeta());

        if (!numeroTarjeta.matches("\\d{13,19}") || !cumpleLuhn(numeroTarjeta)) {
            throw new IllegalArgumentException("El numero de tarjeta no es valido");
        }

        if (!cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("El CVV debe tener 3 o 4 digitos");
        }

        if (nombreTarjeta.length() < 3) {
            throw new IllegalArgumentException("Ingresa el nombre como aparece en la tarjeta");
        }

        validarVencimiento(pagoRequest.getVencimiento());
    }

    private void validarVencimiento(String vencimiento) {
        try {
            YearMonth fechaVencimiento = YearMonth.parse(
                    limpiar(vencimiento),
                    DateTimeFormatter.ofPattern("MM/yy", Locale.ROOT)
            );
            if (fechaVencimiento.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException("La tarjeta esta vencida");
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("El vencimiento debe tener formato MM/YY");
        }
    }

    private void validarOperacionDigital(PagoRequest pagoRequest) {
        String numeroYapePlin = limpiar(pagoRequest.getNumeroYapePlin()).replaceAll("\\s+", "");
        String numeroOperacion = limpiar(pagoRequest.getNumeroOperacion());
        if (!numeroYapePlin.matches("9\\d{8}")) {
            throw new IllegalArgumentException("Ingresa el numero de celular Yape/Plin valido");
        }
        if (!numeroOperacion.matches("\\d{6,12}")) {
            throw new IllegalArgumentException("Ingresa un numero de operacion Yape/Plin valido");
        }
    }

    private boolean cumpleLuhn(String numeroTarjeta) {
        int suma = 0;
        boolean duplicar = false;

        for (int indice = numeroTarjeta.length() - 1; indice >= 0; indice--) {
            int digito = Character.getNumericValue(numeroTarjeta.charAt(indice));
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }

        return suma % 10 == 0;
    }

    private String obtenerUltimosDigitos(String numeroTarjeta) {
        String numeroLimpio = limpiar(numeroTarjeta).replaceAll("\\s+", "");
        return numeroLimpio.substring(numeroLimpio.length() - 4);
    }

    private String generarCodigoTransaccion(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String normalizar(String valor) {
        return limpiar(valor).toUpperCase(Locale.ROOT);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
