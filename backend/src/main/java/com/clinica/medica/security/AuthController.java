package com.clinica.medica.security;

import com.clinica.medica.usuario.dto.UsuarioResponse;
import com.clinica.medica.usuario.model.Usuario;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record LoginRequest(@NotBlank @Size(max=50) String nombreUsuario,
                               @NotBlank @Size(max=72) String contrasena) {}
    public record LoginResponse(String token, String tipo, long expiraEn, UsuarioResponse usuario) {}
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final JwtEncoder encoder;
    private final long ttl;
    private final String dummyHash;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwords, JwtEncoder encoder,
                          @Value("${security.jwt.ttl-seconds:3600}") long ttl) {
        if (ttl < 1) throw new IllegalArgumentException("JWT TTL debe ser positivo");
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.encoder = encoder;
        this.ttl = ttl;
        this.dummyHash = passwords.encode("credencial-inexistente");
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        Usuario u = usuarios.findByNombreUsuario(r.nombreUsuario().toLowerCase(Locale.ROOT)).orElse(null);
        boolean valida = r.contrasena().getBytes(StandardCharsets.UTF_8).length <= 72 &&
                passwords.matches(r.contrasena(), u == null ? dummyHash : u.getContrasenaHash());
        if (!valida || u == null || !u.puedeAutenticarse())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("clinica-medica").subject(u.getNombreUsuario())
                .issuedAt(ahora).expiresAt(ahora.plusSeconds(ttl)).claim("uid", u.getId().toString()).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", ttl, UsuarioResponse.de(u));
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public UsuarioResponse me(Authentication auth) {
        return UsuarioResponse.de(usuarios.findByNombreUsuario(auth.getName()).orElseThrow());
    }
}
