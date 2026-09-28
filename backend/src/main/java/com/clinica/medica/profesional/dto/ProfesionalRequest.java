package com.clinica.medica.profesional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfesionalRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
        String apellido,

        @NotBlank(message = "La matrícula es obligatoria")
        @Size(max = 50, message = "La matrícula no puede superar los 50 caracteres")
        String matricula,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        @Email(message = "El correo electrónico no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email

) {
}