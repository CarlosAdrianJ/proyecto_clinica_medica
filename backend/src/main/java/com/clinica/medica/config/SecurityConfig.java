package com.clinica.medica.config;

import com.clinica.medica.security.UsuarioJwtConverter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecretKey jwtKey(
            @Value("${security.jwt.secret}") String secret
    ) {
        byte[] bytes = Base64.getDecoder().decode(secret);

        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET requiere al menos 32 bytes en Base64"
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("clinica-medica")
        );

        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}")
            String origenesPermitidos
    ) {
        List<String> origenes = Arrays.stream(
                        origenesPermitidos.split(",")
                )
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList();

        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(origenes);
        configuracion.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
        configuracion.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));
        configuracion.setAllowCredentials(false);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource origenConfigurado =
                new UrlBasedCorsConfigurationSource();
        origenConfigurado.registerCorsConfiguration(
                "/**",
                configuracion
        );

        return origenConfigurado;
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UsuarioJwtConverter converter
    ) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/assets/**"
                        ).permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/registro-paciente"
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth/me"
                        ).authenticated()
                        .requestMatchers(
                                "/api/usuarios",
                                "/api/usuarios/**"
                        ).hasRole("ADMINISTRADOR")

                        // El paciente puede consultar solamente su propio perfil.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/pacientes/mi-perfil"
                        ).hasRole("PACIENTE")

                        // Las demás rutas de pacientes siguen restringidas.
                        .requestMatchers(
                                "/api/pacientes",
                                "/api/pacientes/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA"
                        )
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/profesionales",
                                "/api/profesionales/**",
                                "/api/especialidades",
                                "/api/especialidades/**",
                                "/api/horarios-atencion",
                                "/api/horarios-atencion/**",
                                "/api/disponibilidad"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA",
                                "PROFESIONAL"
                        )
                        .requestMatchers(
                                "/api/profesionales",
                                "/api/profesionales/**",
                                "/api/especialidades",
                                "/api/especialidades/**",
                                "/api/horarios-atencion",
                                "/api/horarios-atencion/**"
                        ).hasRole("ADMINISTRADOR")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/turnos/mi-agenda"
                        ).hasRole("PROFESIONAL")
                        .requestMatchers(
                                "/api/turnos",
                                "/api/turnos/**"
                        ).hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA"
                        )
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(converter)
                        )
                )
                .build();
    }
}