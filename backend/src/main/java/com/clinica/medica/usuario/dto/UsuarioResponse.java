package com.clinica.medica.usuario.dto;

import com.clinica.medica.usuario.model.Rol;
import com.clinica.medica.usuario.model.Usuario;

public record UsuarioResponse(Long id, String nombreUsuario, Rol rol, Long profesionalId, boolean activo) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombreUsuario(), u.getRol(),
                u.getProfesional() == null ? null : u.getProfesional().getId(), u.isActivo());
    }
}
