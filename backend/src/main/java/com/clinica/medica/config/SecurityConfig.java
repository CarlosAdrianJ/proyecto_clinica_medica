package com.clinica.medica.config;

import com.clinica.medica.security.UsuarioJwtConverter;
import jakarta.servlet.DispatcherType;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean SecretKey jwtKey(@Value("${security.jwt.secret}") String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET requiere al menos 32 bytes en Base64");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
    @Bean JwtEncoder jwtEncoder(SecretKey key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
    @Bean JwtDecoder jwtDecoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("clinica-medica"));
        return decoder;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioJwtConverter converter) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    .requestMatchers("/api/auth/me").authenticated()
                    .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMINISTRADOR")
                    .requestMatchers("/api/pacientes", "/api/pacientes/**").hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                    .requestMatchers(HttpMethod.GET, "/api/profesionales", "/api/profesionales/**",
                            "/api/especialidades", "/api/especialidades/**",
                            "/api/horarios-atencion", "/api/horarios-atencion/**", "/api/disponibilidad")
                        .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "PROFESIONAL")
                        .requestMatchers("/api/profesionales", "/api/profesionales/**", "/api/especialidades",
                                "/api/especialidades/**", "/api/horarios-atencion", "/api/horarios-atencion/**")
                            .hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/turnos", "/api/turnos/**")
                            .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(converter)))
                .build();
    }
}
