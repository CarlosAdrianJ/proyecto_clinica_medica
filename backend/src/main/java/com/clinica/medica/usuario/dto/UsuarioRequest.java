package com.clinica.medica.usuario.dto;

import com.clinica.medica.usuario.model.Rol;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{3,50}") String nombreUsuario,
        @NotBlank @Size(min = 8, max = 72) String contrasena,
        @NotNull Rol rol,
        @Positive Long profesionalId,
        @NotNull Boolean activo
) {}
