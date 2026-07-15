package com.utp.lenasysabores.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotBlank(message = "Ingresa tu correo")
    @Email(message = "Ingresa un correo valido")
    private String correo;

    @NotBlank(message = "Ingresa tu contraseña")
    private String password;
}
