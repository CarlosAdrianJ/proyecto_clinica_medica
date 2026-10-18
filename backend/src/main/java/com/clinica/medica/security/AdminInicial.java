package com.clinica.medica.security;

import com.clinica.medica.usuario.model.*;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Aprovisionamiento inicial optativo, unicamente sobre una tabla vacia. */
@Component
public class AdminInicial implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String nombre;
    private final String password;
    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${security.bootstrap.username:}") String nombre,
            @Value("${security.bootstrap.password:}") String password) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.nombre = nombre;
        this.password = password;
    }
    @Override @Transactional
    public void run(String... args) {
        if (nombre.isEmpty() && password.isEmpty()) return;
        if (usuarios.count() != 0) return;
        if (!nombre.matches("[a-zA-Z0-9._-]{3,50}") || password.length() < 8 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72 || password.isBlank())
            throw new IllegalArgumentException("Credenciales iniciales invalidas");
        Usuario u = new Usuario();
        u.setNombreUsuario(nombre.toLowerCase(Locale.ROOT));
        u.setContrasenaHash(encoder.encode(password));
        u.setRol(Rol.ADMINISTRADOR);
        usuarios.save(u);
    }
}
