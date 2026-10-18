package com.clinica.medica.usuario.service;

import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.usuario.dto.*;
import com.clinica.medica.usuario.model.*;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final ProfesionalRepository profesionales;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, ProfesionalRepository profesionales, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.profesionales = profesionales;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() { return usuarios.findAll().stream().map(UsuarioResponse::de).toList(); }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) { return UsuarioResponse.de(obtener(id)); }

    public UsuarioResponse crear(UsuarioRequest request) { return guardar(new Usuario(), request); }
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) { return guardar(obtener(id), request); }
    public void desactivar(Long id) { obtener(id).setActivo(false); }

    private Usuario obtener(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioResponse guardar(Usuario usuario, UsuarioRequest r) {
        String nombre = r.nombreUsuario().toLowerCase(Locale.ROOT);
        usuarios.findByNombreUsuario(nombre).filter(u -> !u.getId().equals(usuario.getId())).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nombre de usuario en uso");
        });
        if (r.contrasena().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasena supera los 72 bytes de BCrypt");
        }
        Profesional profesional = null;
        if (r.rol() == Rol.PROFESIONAL) {
            if (r.profesionalId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol PROFESIONAL requiere profesionalId");
            profesional = profesionales.findById(r.profesionalId())
                    .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profesional inexistente o inactivo"));
        } else if (r.profesionalId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo PROFESIONAL puede tener profesionalId");
        }
        usuario.setNombreUsuario(nombre);
        usuario.setContrasenaHash(encoder.encode(r.contrasena()));
        usuario.setRol(r.rol());
        usuario.setProfesional(profesional);
        usuario.setActivo(r.activo());
        try { return UsuarioResponse.de(usuarios.saveAndFlush(usuario)); }
        catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Datos de usuario en conflicto");
        }
    }
}
