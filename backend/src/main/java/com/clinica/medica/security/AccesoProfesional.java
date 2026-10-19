package com.clinica.medica.security;

import com.clinica.medica.usuario.model.Rol;
import com.clinica.medica.usuario.model.Usuario;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component("accesoProfesional")
public class AccesoProfesional {

    private final UsuarioRepository usuarios;

    public AccesoProfesional(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public boolean puedeConsultar(
            Authentication auth,
            Long profesionalId
    ) {
        return usuarios.findByNombreUsuario(auth.getName())
                .filter(Usuario::puedeAutenticarse)
                .filter(usuario -> usuario.getRol() == Rol.PROFESIONAL)
                .filter(usuario ->
                        usuario.getProfesional().getId().equals(profesionalId)
                )
                .isPresent();
    }

    @Transactional(readOnly = true)
    public Long obtenerProfesionalId(Authentication auth) {
        return usuarios.findByNombreUsuario(auth.getName())
                .filter(Usuario::puedeAutenticarse)
                .filter(usuario -> usuario.getRol() == Rol.PROFESIONAL)
                .map(usuario -> usuario.getProfesional().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El usuario no tiene un profesional asociado"
                ));
    }
}