package com.utp.lenasysabores.service;

import com.utp.lenasysabores.model.RegistroForm;
import com.utp.lenasysabores.model.Usuario;
import com.utp.lenasysabores.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Registra un nuevo usuario en la BD.
     * Retorna null si el correo ya está registrado,
     * o el Usuario guardado si el registro fue exitoso.
     */
    public Usuario registrar(RegistroForm form) {
        // Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(form.getCorreo()).isPresent()) {
            return null; // correo duplicado
        }

        if (usuarioRepository.findByDni(form.getDni()).isPresent()) {
            return null; // DNI duplicado
        }

        Usuario usuario = new Usuario();
        usuario.setDni(form.getDni());
        usuario.setNombre(form.getNombre());
        usuario.setCorreo(form.getCorreo());
        // Guardamos la contraseña encriptada con BCrypt
        usuario.setPassword(passwordEncoder.encode(form.getPassword()));
        usuario.setRol("CLIENTE");

        return usuarioRepository.save(usuario);
    }

    public boolean existeCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).isPresent();
    }

    public boolean existeDni(String dni) {
        return usuarioRepository.findByDni(dni).isPresent();
    }

    /**
     * Valida credenciales para el login.
     * Retorna el Usuario si las credenciales son correctas, o null si no.
     */
    public Usuario autenticar(String correo, String password) {
        Optional<Usuario> optUsuario = usuarioRepository.findByCorreo(correo);

        if (optUsuario.isEmpty()) {
            return null; // usuario no encontrado
        }

        Usuario usuario = optUsuario.get();

        // Compara la contraseña ingresada con el hash guardado en BD
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return null; // contraseña incorrecta
        }

        return usuario;
    }
}
