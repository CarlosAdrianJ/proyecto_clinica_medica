package com.clinica.medica.security;

import com.clinica.medica.usuario.model.Rol;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("accesoProfesional")
public class AccesoProfesional {
    private final UsuarioRepository usuarios;
    public AccesoProfesional(UsuarioRepository usuarios) { this.usuarios = usuarios; }
    @Transactional(readOnly = true)
    public boolean puedeConsultar(Authentication auth, Long profesionalId) {
        return usuarios.findByNombreUsuario(auth.getName())
                .filter(u -> u.puedeAutenticarse() && u.getRol() == Rol.PROFESIONAL)
                .filter(u -> u.getProfesional().getId().equals(profesionalId)).isPresent();
    }
}
