package com.clinica.medica.usuario.dto;

import com.clinica.medica.usuario.model.Rol;
import com.clinica.medica.usuario.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombreUsuario,
        Rol rol,
        Long profesionalId,
        Long pacienteId,
        boolean activo
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                usuario.getProfesional() == null
                        ? null
                        : usuario.getProfesional().getId(),
                usuario.getPaciente() == null
                        ? null
                        : usuario.getPaciente().getId(),
                usuario.isActivo()
        );
    }
}