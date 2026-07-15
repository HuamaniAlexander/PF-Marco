package com.utp.lenasysabores.controller;

import com.utp.lenasysabores.model.Producto;
import com.utp.lenasysabores.service.CatalogoService;
import com.utp.lenasysabores.service.ReservaService;
import com.utp.lenasysabores.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CatalogoService catalogoService;
    private final ReservaService reservaService;
    private final VentaService ventaService;

    public AdminController(CatalogoService catalogoService,
                           ReservaService reservaService,
                           VentaService ventaService) {
        this.catalogoService = catalogoService;
        this.reservaService = reservaService;
        this.ventaService = ventaService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        return "redirect:/productos/nuevo";
    }

    @GetMapping("/productos/nuevo")
    public String nuevoProducto(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        return "redirect:/productos/nuevo";
    }

    @PostMapping("/productos/nuevo")
    public String guardarProducto(@ModelAttribute("producto") Producto producto,
                                  HttpSession session,
                                  RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        catalogoService.guardarProducto(producto);
        redirectAttrs.addFlashAttribute("mensajeAdmin", "Producto agregado correctamente.");
        return "redirect:/admin/dashboard#productos";
    }

    @GetMapping("/productos/editar/{id}")
    public String editarProducto(@PathVariable Integer id,
                                 HttpSession session,
                                 Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        return "redirect:/productos/editar/" + id;
    }

    @PostMapping("/productos/editar")
    public String actualizarProducto(@ModelAttribute("producto") Producto producto,
                                     HttpSession session,
                                     RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        catalogoService.guardarProducto(producto);
        redirectAttrs.addFlashAttribute("mensajeAdmin", "Producto actualizado correctamente.");
        return "redirect:/admin/dashboard#productos";
    }

    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        catalogoService.eliminarProducto(id);
        redirectAttrs.addFlashAttribute("mensajeAdmin", "Producto eliminado correctamente.");
        return "redirect:/admin/dashboard#productos";
    }

    @PostMapping("/reservas/eliminar/{id}")
    public String eliminarReserva(@PathVariable Integer id,
                                  HttpSession session,
                                  RedirectAttributes redirectAttrs) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        reservaService.eliminar(id);
        redirectAttrs.addFlashAttribute("mensajeAdmin", "Reserva eliminada correctamente.");
        return "redirect:/admin/dashboard#reservas";
    }

    private void cargarDatosDashboard(Model model) {
        var productos = catalogoService.obtenerProductos();
        var reservas = reservaService.listar();
        var ventas = ventaService.listarTodas();
        double ingresos = ventas.stream()
                .filter(venta -> "PAGADO".equals(venta.getEstadoPago()))
                .mapToDouble(venta -> venta.getTotal() == null ? 0 : venta.getTotal())
                .sum();

        model.addAttribute("productos", productos);
        model.addAttribute("reservas", reservas);
        model.addAttribute("ventas", ventas);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("totalReservas", reservas.size());
        model.addAttribute("totalVentas", ventas.size());
        model.addAttribute("ingresos", ingresos);
    }

    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}
