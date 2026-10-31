package com.clinica.medica.usuario.service;

import com.clinica.medica.paciente.model.Paciente;
import com.clinica.medica.paciente.repository.PacienteRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.usuario.dto.UsuarioRequest;
import com.clinica.medica.usuario.dto.UsuarioResponse;
import com.clinica.medica.usuario.model.Rol;
import com.clinica.medica.usuario.model.Usuario;
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
    private final PacienteRepository pacientes;
    private final PasswordEncoder encoder;

    public UsuarioService(
            UsuarioRepository usuarios,
            ProfesionalRepository profesionales,
            PacienteRepository pacientes,
            PasswordEncoder encoder
    ) {
        this.usuarios = usuarios;
        this.profesionales = profesionales;
        this.pacientes = pacientes;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll()
                .stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.de(obtener(id));
    }

    public UsuarioResponse crear(UsuarioRequest request) {
        return guardar(new Usuario(), request);
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        return guardar(obtener(id), request);
    }

    public void desactivar(Long id) {
        obtener(id).setActivo(false);
    }

    private Usuario obtener(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    private UsuarioResponse guardar(Usuario usuario, UsuarioRequest request) {
        String nombre = request.nombreUsuario()
                .toLowerCase(Locale.ROOT);

        usuarios.findByNombreUsuario(nombre)
                .filter(otro -> !otro.getId().equals(usuario.getId()))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Nombre de usuario en uso"
                    );
                });

        if (request.contrasena()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña supera los 72 bytes permitidos"
            );
        }

        Profesional profesional = null;
        Paciente paciente = null;

        if (request.rol() == Rol.PROFESIONAL) {
            if (request.profesionalId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El rol PROFESIONAL requiere profesionalId"
                );
            }

            profesional = profesionales.findById(request.profesionalId())
                    .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Profesional inexistente o inactivo"
                    ));
        } else if (request.profesionalId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo PROFESIONAL puede tener profesionalId"
            );
        }

        if (request.rol() == Rol.PACIENTE) {
            if (request.pacienteId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El rol PACIENTE requiere una ficha de paciente"
                );
            }

            paciente = pacientes.findByIdAndActivoTrue(request.pacienteId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Paciente inexistente o inactivo"
                    ));

            boolean yaVinculado = usuario.getId() == null
                    ? usuarios.existsByPacienteId(paciente.getId())
                    : usuarios.existsByPacienteIdAndIdNot(
                            paciente.getId(),
                            usuario.getId()
                    );

            if (yaVinculado) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Ese paciente ya tiene una cuenta vinculada"
                );
            }
        } else if (request.pacienteId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo PACIENTE puede tener pacienteId"
            );
        }

        usuario.setNombreUsuario(nombre);
        usuario.setContrasenaHash(encoder.encode(request.contrasena()));
        usuario.setRol(request.rol());
        usuario.setProfesional(profesional);
        usuario.setPaciente(paciente);
        usuario.setActivo(request.activo());

        try {
            return UsuarioResponse.de(usuarios.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException error) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Datos de usuario en conflicto"
            );
        }
    }
}