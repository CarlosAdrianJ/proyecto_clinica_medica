package com.clinica.medica.security;

import com.clinica.medica.paciente.model.Paciente;
import com.clinica.medica.paciente.repository.PacienteRepository;
import com.clinica.medica.usuario.dto.UsuarioResponse;
import com.clinica.medica.usuario.model.Rol;
import com.clinica.medica.usuario.model.Usuario;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record LoginRequest(
            @NotBlank @Size(max = 50) String nombreUsuario,
            @NotBlank @Size(max = 72) String contrasena
    ) {
    }

    public record LoginResponse(
            String token,
            String tipo,
            long expiraEn,
            UsuarioResponse usuario
    ) {
    }

    public record RegistroPacienteRequest(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 100) String apellido,
            @NotBlank @Size(max = 20) String dni,
            @Past LocalDate fechaNacimiento,
            @Size(max = 30) String telefono,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 200) String direccion,
            @NotBlank
            @Pattern(regexp = "[a-zA-Z0-9._-]{4,50}")
            String nombreUsuario,
            @NotBlank @Size(min = 10, max = 72) String contrasena
    ) {
    }

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final long ttl;
    private final String dummyHash;

    public AuthController(
            UsuarioRepository usuarios,
            PacienteRepository pacientes,
            PasswordEncoder passwords,
            JwtEncoder encoder,
            @Value("${security.jwt.ttl-seconds:3600}") long ttl
    ) {
        if (ttl < 1) {
            throw new IllegalArgumentException("JWT TTL debe ser positivo");
        }

        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.passwords = passwords;
        this.encoder = encoder;
        this.ttl = ttl;
        this.dummyHash = passwords.encode("credencial-inexistente");
    }

    @PostMapping("/registro-paciente")
    @Transactional
    public LoginResponse registrarPaciente(
            @Valid @RequestBody RegistroPacienteRequest request
    ) {
        String nombreUsuario = request.nombreUsuario()
                .trim()
                .toLowerCase(Locale.ROOT);
        String dni = request.dni().trim();

        if (request.contrasena()
                .getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña supera el límite permitido"
            );
        }

        if (usuarios.existsByNombreUsuario(nombreUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ese nombre de usuario ya está en uso"
            );
        }

        if (pacientes.existsByDni(dni)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una ficha con ese DNI. Comunicate con la clínica para vincular tu acceso sin duplicar tus datos."
            );
        }

        Paciente paciente = new Paciente();
        paciente.setNombre(request.nombre().trim());
        paciente.setApellido(request.apellido().trim());
        paciente.setDni(dni);
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setTelefono(
                request.telefono() == null
                        ? null
                        : request.telefono().trim()
        );
        paciente.setEmail(request.email()
                .trim()
                .toLowerCase(Locale.ROOT));
        paciente.setDireccion(
                request.direccion() == null
                        ? null
                        : request.direccion().trim()
        );

        paciente = pacientes.save(paciente);

        Usuario cuenta = new Usuario();
        cuenta.setNombreUsuario(nombreUsuario);
        cuenta.setContrasenaHash(passwords.encode(request.contrasena()));
        cuenta.setRol(Rol.PACIENTE);
        cuenta.setPaciente(paciente);
        cuenta.setActivo(true);

        cuenta = usuarios.save(cuenta);

        return crearRespuestaLogin(cuenta);
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        Usuario usuario = usuarios
                .findByNombreUsuario(
                        request.nombreUsuario().toLowerCase(Locale.ROOT)
                )
                .orElse(null);

        boolean contrasenaValida =
                request.contrasena()
                        .getBytes(StandardCharsets.UTF_8).length <= 72
                && passwords.matches(
                        request.contrasena(),
                        usuario == null
                                ? dummyHash
                                : usuario.getContrasenaHash()
                );

        if (!contrasenaValida
                || usuario == null
                || !usuario.puedeAutenticarse()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas"
            );
        }

        return crearRespuestaLogin(usuario);
    }

    private LoginResponse crearRespuestaLogin(Usuario usuario) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("clinica-medica")
                .subject(usuario.getNombreUsuario())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(ttl))
                .claim("uid", usuario.getId().toString())
                .build();

        String token = encoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();

        return new LoginResponse(
                token,
                "Bearer",
                ttl,
                UsuarioResponse.de(usuario)
        );
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public UsuarioResponse me(Authentication authentication) {
        return UsuarioResponse.de(
                usuarios.findByNombreUsuario(authentication.getName())
                        .orElseThrow()
        );
    }
}