package com.utp.lenasysabores.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.utp.lenasysabores.model.Venta;
import com.utp.lenasysabores.service.CatalogoService;
import com.utp.lenasysabores.service.VentaService;

import java.util.List;
import java.util.Objects;

@Controller
public class HomeController {

    private final CatalogoService catalogoService;
    private final VentaService ventaService;

    public HomeController(CatalogoService catalogoService, VentaService ventaService) {
        this.catalogoService = catalogoService;
        this.ventaService = ventaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("promociones", catalogoService.obtenerPromociones());
        return "index";
    }

    @GetMapping("/carta")
    public String carta(Model model) {
        model.addAttribute("productos", catalogoService.obtenerProductos());
        return "carta";
    }

    @GetMapping("/promociones")
    public String promociones(Model model) {
        model.addAttribute("promociones", catalogoService.obtenerPromociones());
        return "promociones";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        List<Venta> ventas = ventaService.obtenerVentasDeUsuario(usuarioId);
        Venta pedidoActual = ventas.stream().findFirst().orElse(null);
        List<String> direcciones = ventas.stream()
                .map(Venta::getDireccionEntrega)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(direccion -> !direccion.isBlank())
                .distinct()
                .toList();

        model.addAttribute("ventas", ventas);
        model.addAttribute("pedidoActual", pedidoActual);
        model.addAttribute("direcciones", direcciones);
        return "perfil";
    }

    @GetMapping("/mi-carrito")
    public String miCarrito(HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        return "carrito";
    }
}
