package com.utp.lenasysabores.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.utp.lenasysabores.model.Producto;
import com.utp.lenasysabores.service.CatalogoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/productos")
public class ProductoAdminController {

    private final CatalogoService catalogoService;

    public ProductoAdminController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/nuevo")
    public String nuevoProducto(HttpSession session, Model model) {
        if (!esAdmin(session)) {
            return "redirect:/";
        }

        model.addAttribute("producto", new Producto());
        model.addAttribute("esEditar", false);

        return "producto-form";
    }

    @PostMapping("/nuevo")
    public String guardarProducto(
            @ModelAttribute("producto") Producto producto, HttpSession session) {

        if (!esAdmin(session)) {
            return "redirect:/";
        }

        catalogoService.guardarProducto(producto);
        return "redirect:/carta";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable("id") Integer id, HttpSession session, Model model) {

        if (!esAdmin(session)) {return "redirect:/";}

        Producto producto = catalogoService.obtenerProductoPorId(id);
        model.addAttribute("producto", producto);
        model.addAttribute("esEditar", true);

        return "producto-form";
    }

    @PostMapping("/editar")
    public String actualizarProducto(
            @ModelAttribute("producto") Producto producto, HttpSession session) {

        if (!esAdmin(session)) {return "redirect:/";}

        catalogoService.guardarProducto(producto);
        return "redirect:/carta";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProducto( @PathVariable("id") Integer id,HttpSession session) {

        if (!esAdmin(session)) {return "redirect:/";}

        catalogoService.eliminarProducto(id);
        return "redirect:/carta";
    }
    private boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }
}
