package com.utp.lenasysabores.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Selecciona una fecha")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotBlank(message = "Selecciona una hora")
    @Column(length = 20, nullable = false)
    private String hora = "1:00pm";

    @NotNull(message = "Indica el numero de personas")
    @Min(value = 1, message = "Debe ser minimo 1 persona")
    @Max(value = 20, message = "El maximo es 20 personas")
    @Column(nullable = false)
    private Integer personas = 2;

    @NotBlank(message = "Ingresa tu nombre")
    @Column(length = 100, nullable = false)
    private String nombre;

    @NotBlank(message = "Ingresa tu correo")
    @Email(message = "Ingresa un correo valido")
    @Column(length = 100, nullable = false)
    private String correo;

    @NotBlank(message = "Ingresa tu telefono")
    @Pattern(
        regexp = "^\\+?[0-9]{7,15}$",
        message = "Ingresa un número de teléfono válido"
    )
    @Column(length = 20, nullable = false)
    private String telefono;

    @Column(length = 100)
    private String motivo;

    @Column(length = 500)
    private String comentario;

    public Reserva(Integer id, LocalDate fecha, String hora, Integer personas,
                   String nombre, String correo, String telefono,
                   String motivo, String comentario) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.personas = personas;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.motivo = motivo;
        this.comentario = comentario;
    }

    
}
