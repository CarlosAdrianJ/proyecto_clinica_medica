package com.clinica.medica.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @NotBlank(message = "El DNI es obligatorio")
        @Size(max = 20, message = "El DNI no puede superar los 20 caracteres")
        String dni,

        @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
        LocalDate fechaNacimiento,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion
) {
}