package com.utp.lenasysabores.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.lenasysabores.model.LoginForm;
import com.utp.lenasysabores.model.Reserva;
import com.utp.lenasysabores.service.ReservaService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reserva")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public String reserva(Model model, HttpSession session) {
        if (session.getAttribute("usuarioId") == null) {
            model.addAttribute("mensaje", "Debes iniciar sesión para hacer una reserva.");
            model.addAttribute("loginForm", new LoginForm());
            return "login";
        }

        model.addAttribute("reserva", new Reserva());
        return "reserva";
    }

    @PostMapping
    public String guardarReserva(
            @Valid @ModelAttribute("reserva") Reserva reserva,
            BindingResult result,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttrs) {

        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("mensaje", "Corrige los errores marcados en el formulario.");
            return "reserva";
        }

        reservaService.registrar(reserva);

        redirectAttrs.addFlashAttribute(
                "mensaje",
                "Reserva registrada correctamente para " + reserva.getNombre());

        return "redirect:/reserva";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarReserva(@PathVariable("id") Integer id, HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");

        if (!"ADMIN".equals(rol)) {
            return "redirect:/";
        }

        reservaService.eliminar(id);
        return "redirect:/admin/dashboard#reservas";
    }
}
