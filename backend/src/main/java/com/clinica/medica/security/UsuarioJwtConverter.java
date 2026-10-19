package com.clinica.medica.security;

import com.clinica.medica.usuario.model.Usuario;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
public class UsuarioJwtConverter implements org.springframework.core.convert.converter.Converter<Jwt, JwtAuthenticationToken> {
    private final UsuarioRepository usuarios;
    public UsuarioJwtConverter(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override
    @Transactional(readOnly = true)
    public JwtAuthenticationToken convert(Jwt jwt) {
        Usuario u = usuarios.findByNombreUsuario(jwt.getSubject())
                .filter(Usuario::puedeAutenticarse)
                .filter(usuario -> usuario.getId().toString().equals(jwt.getClaimAsString("uid")))
                .orElseThrow(() -> new BadCredentialsException("Cuenta no disponible"));
        return new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())), u.getNombreUsuario());
    }
}
