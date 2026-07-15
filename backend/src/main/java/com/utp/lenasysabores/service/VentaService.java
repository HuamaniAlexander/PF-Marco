package com.utp.lenasysabores.service;

import com.utp.lenasysabores.model.CarritoItem;
import com.utp.lenasysabores.model.PagoResultado;
import com.utp.lenasysabores.model.Venta;
import com.utp.lenasysabores.model.VentaItem;
import com.utp.lenasysabores.repository.VentaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;

    public VentaService(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    /**
     * Convierte el carrito de la sesión en una Venta y la guarda en BD.
     * Recibe los datos del usuario desde la sesión.
     */
    public Venta confirmarPedido(List<CarritoItem> carrito,
                                  Long usuarioId,
                                  String usuarioNombre,
                                  String usuarioCorreo,
                                  PagoResultado pagoResultado) {

        // 1. Crear la cabecera de la venta
        Venta venta = new Venta();
        venta.setUsuarioId(usuarioId);
        venta.setUsuarioNombre(usuarioNombre);
        venta.setUsuarioCorreo(usuarioCorreo);
        venta.setEstado("CONFIRMADO");
        venta.setMetodoPago(pagoResultado.getMetodoPago());
        venta.setEstadoPago(pagoResultado.getEstadoPago());
        venta.setCodigoTransaccion(pagoResultado.getCodigoTransaccion());
        venta.setTarjetaUltimosDigitos(pagoResultado.getTarjetaUltimosDigitos());
        venta.setMetodoEntrega(pagoResultado.getMetodoEntrega());
        venta.setDireccionEntrega(pagoResultado.getDireccionEntrega());

        // 2. Convertir cada CarritoItem en un VentaItem
        for (CarritoItem item : carrito) {
            VentaItem ventaItem = new VentaItem();
            ventaItem.setProductoId(item.getProductoId());
            ventaItem.setProductoNombre(item.getNombre());
            ventaItem.setPrecio(item.getPrecio());
            ventaItem.setCantidad(item.getCantidad());
            ventaItem.setSubtotal(item.getSubtotal());
            ventaItem.setVenta(venta); // ← enlazar con la venta padre

            venta.getItems().add(ventaItem);
        }

        // 3. Calcular el total
        double total = carrito.stream()
                .mapToDouble(CarritoItem::getSubtotal)
                .sum();
        venta.setTotal(total);

        // 4. Guardar en BD (guarda Venta + VentaItems por el CascadeType.ALL)
        return ventaRepository.save(venta);
    }

    // Obtener ventas de un usuario (para historial)
    public List<Venta> obtenerVentasDeUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId);
    }

    // Obtener todas las ventas (para administración)
    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public void actualizarEstado(Integer id, String estado, String estadoPago) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        String nuevoEstado = estado == null ? venta.getEstado() : estado.trim().toUpperCase();
        String nuevoEstadoPago = estadoPago == null ? venta.getEstadoPago() : estadoPago.trim().toUpperCase();

        if (!"PAGADO".equals(nuevoEstadoPago)
                && ("EN_PROCESO".equals(nuevoEstado) || "LISTO".equals(nuevoEstado) || "ENTREGADO".equals(nuevoEstado))) {
            throw new IllegalArgumentException("Primero confirma el pago antes de avanzar el pedido");
        }

        if (estado != null && !estado.isBlank()) {
            venta.setEstado(nuevoEstado);
        }

        if (estadoPago != null && !estadoPago.isBlank()) {
            venta.setEstadoPago(nuevoEstadoPago);
        }

        ventaRepository.save(venta);
    }

    public void eliminar(Integer id) {
        ventaRepository.deleteById(id);
    }
}
