package com.utp.lenasysabores.repository;

import com.utp.lenasysabores.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca un usuario por correo (para login y validar duplicados)
    Optional<Usuario> findByCorreo(String correo);

    Optional<Usuario> findByDni(String dni);
}
