package com.utp.lenasysabores.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.utp.lenasysabores.model.LoginForm;
import com.utp.lenasysabores.model.RegistroForm;
import com.utp.lenasysabores.model.Usuario;
import com.utp.lenasysabores.service.ReniecService;
import com.utp.lenasysabores.service.UsuarioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Map;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ReniecService reniecService;

    public AuthController(UsuarioService usuarioService, ReniecService reniecService) {
        this.usuarioService = usuarioService;
        this.reniecService = reniecService;
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        if (session.getAttribute("usuarioId") != null) {
            return "redirect:/";
        }

        model.addAttribute("loginForm", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult result,
            Model model,
            HttpSession session) {

        if (result.hasErrors()) {
            return "login";
        }

        Usuario usuario = usuarioService.autenticar(
                loginForm.getCorreo(),
                loginForm.getPassword());

        if (usuario == null) {
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            return "login";
        }

        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombre());
        session.setAttribute("usuarioCorreo", usuario.getCorreo());
        session.setAttribute("usuarioRol", usuario.getRol());

        return "redirect:/";
    }

    @GetMapping("/registro")
    public String registro(Model model, HttpSession session) {
        if (session.getAttribute("usuarioId") != null) {
            return "redirect:/";
        }

        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("registroForm") RegistroForm registroForm,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            return "registro";
        }

        if (!registroForm.getPassword().equals(registroForm.getConfirmarPassword())) {
            model.addAttribute("errorPassword", "Las contraseñas no coinciden.");
            return "registro";
        }

        if (usuarioService.existeDni(registroForm.getDni())) {
            model.addAttribute("errorDni", "Este DNI ya esta registrado.");
            return "registro";
        }

        if (usuarioService.existeCorreo(registroForm.getCorreo())) {
            model.addAttribute("errorCorreo", "Este correo ya esta registrado.");
            return "registro";
        }

        Usuario nuevo = usuarioService.registrar(registroForm);

        if (nuevo == null) {
            model.addAttribute("errorCorreo", "Este correo ya está registrado.");
            return "registro";
        }

        redirectAttrs.addFlashAttribute("mensajeExito",
                "¡Cuenta creada exitosamente! Ya puedes iniciar sesión.");

        return "redirect:/login";
    }

    @GetMapping("/api/reniec/dni")
    @ResponseBody
    public ResponseEntity<?> consultarDni(@RequestParam("numero") String numero) {
        if (numero == null || !numero.matches("\\d{8}")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El DNI debe tener 8 digitos."));
        }

        return reniecService.consultarPorDni(numero)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontraron datos para el DNI ingresado.")));
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttrs) {
        session.invalidate();
        redirectAttrs.addFlashAttribute("mensajeExito", "Sesión cerrada correctamente.");
        return "redirect:/login";
    }
}
