package com.utp.lenasysabores.controller;

import com.utp.lenasysabores.model.CarritoItem;
import com.utp.lenasysabores.model.PagoRequest;
import com.utp.lenasysabores.model.PagoResultado;
import com.utp.lenasysabores.model.Producto;
import com.utp.lenasysabores.model.Venta;
import com.utp.lenasysabores.service.CatalogoService;
import com.utp.lenasysabores.service.PagoService;
import com.utp.lenasysabores.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/carrito")
public class CarritoController {

    private final CatalogoService catalogoService;
    private final VentaService ventaService;
    private final PagoService pagoService;

    public CarritoController(CatalogoService catalogoService,
                             VentaService ventaService,
                             PagoService pagoService) {
        this.catalogoService = catalogoService;
        this.ventaService = ventaService;
        this.pagoService = pagoService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerCarrito(HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Debes iniciar sesion"));
        }
        List<CarritoItem> carrito = obtenerOCrearCarrito(session);
        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        return ResponseEntity.ok(Map.of("items", carrito, "total", total));
    }

    @PostMapping("/agregar/{productoId}")
    public ResponseEntity<?> agregar(@PathVariable Integer productoId,
                                     HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Debes iniciar sesion para agregar al carrito"));
        }

        Producto producto = catalogoService.obtenerProductos()
                .stream()
                .filter(p -> p.getId().equals(productoId))
                .findFirst()
                .orElse(null);

        if (producto == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Producto no encontrado"));
        }

        List<CarritoItem> carrito = obtenerOCrearCarrito(session);

        CarritoItem existente = carrito.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + 1);
        } else {
            carrito.add(new CarritoItem(
                    producto.getId(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getImagenUrl()
            ));
        }

        session.setAttribute("carrito", carrito);
        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();

        return ResponseEntity.ok(Map.of(
                "mensaje", "Producto agregado",
                "cantidad", carrito.stream().mapToInt(CarritoItem::getCantidad).sum(),
                "total", total,
                "items", carrito
        ));
    }

    @DeleteMapping("/quitar/{productoId}")
    public ResponseEntity<?> quitar(@PathVariable Integer productoId,
                                    HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        List<CarritoItem> carrito = obtenerOCrearCarrito(session);
        carrito.removeIf(i -> i.getProductoId().equals(productoId));
        session.setAttribute("carrito", carrito);

        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();
        return ResponseEntity.ok(Map.of(
                "cantidad", carrito.stream().mapToInt(CarritoItem::getCantidad).sum(),
                "total", total,
                "items", carrito
        ));
    }

    @DeleteMapping("/vaciar")
    public ResponseEntity<?> vaciar(HttpSession session) {
        session.setAttribute("carrito", new ArrayList<>());
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado"));
    }

    @PostMapping("/pagar")
    public ResponseEntity<?> pagar(@RequestBody PagoRequest pagoRequest,
                                   HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Debes iniciar sesion"));
        }

        List<CarritoItem> carrito = obtenerOCrearCarrito(session);
        if (carrito.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El carrito esta vacio"));
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String usuarioNombre = (String) session.getAttribute("usuarioNombre");
        String usuarioCorreo = (String) session.getAttribute("usuarioCorreo");
        double total = carrito.stream().mapToDouble(CarritoItem::getSubtotal).sum();

        try {
            PagoResultado pagoResultado = pagoService.procesarPago(pagoRequest, total);
            Venta venta = ventaService.confirmarPedido(
                    carrito, usuarioId, usuarioNombre, usuarioCorreo, pagoResultado);

            session.setAttribute("carrito", new ArrayList<>());

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Pedido confirmado",
                    "ventaId", venta.getId(),
                    "total", venta.getTotal(),
                    "estadoPago", venta.getEstadoPago(),
                    "codigoTransaccion", venta.getCodigoTransaccion()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private List<CarritoItem> obtenerOCrearCarrito(HttpSession session) {
        List<CarritoItem> carrito =
                (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }
}
