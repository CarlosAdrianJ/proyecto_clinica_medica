package com.clinica.medica.security;

import com.clinica.medica.usuario.model.*;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** HTTP real: verifica tambien el despacho ERROR del contenedor Servlet. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:seguridad-http;MODE=MySQL;DB_CLOSE_DELAY=-1")
@DirtiesContext
class SeguridadHttpTest {
    @Value("${local.server.port}") int port;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    private final HttpClient client = HttpClient.newHttpClient();
    HttpResponse<String> enviar(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder r = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) r.header("Authorization", "Bearer " + token);
        r.header("Content-Type", "application/json");
        r.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(r.build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void conservaErroresDeNegocioEnHttpReal() throws Exception {
        Usuario admin = new Usuario();
        admin.setNombreUsuario("http-admin"); admin.setRol(Rol.ADMINISTRADOR);
        admin.setContrasenaHash(encoder.encode("PruebaHttp123!")); usuarios.saveAndFlush(admin);
        HttpResponse<String> login = enviar("POST", "/api/auth/login", null,
                "{\"nombreUsuario\":\"http-admin\",\"contrasena\":\"PruebaHttp123!\"}");
        assertEquals(200, login.statusCode());
        String token = JsonPath.read(login.body(), "$.token");
        assertEquals(409, enviar("POST", "/api/usuarios", token,
                "{\"nombreUsuario\":\"http-admin\",\"contrasena\":\"PruebaHttp123!\",\"rol\":\"ADMINISTRADOR\",\"activo\":true}").statusCode());
        assertEquals(400, enviar("POST", "/api/usuarios", token,
                "{\"nombreUsuario\":\"sin-prof\",\"contrasena\":\"PruebaHttp123!\",\"rol\":\"PROFESIONAL\",\"activo\":true}").statusCode());
        assertEquals(404, enviar("GET", "/api/usuarios/999999", token, null).statusCode());
        assertEquals(401, enviar("GET", "/api/usuarios", null, null).statusCode());
        // La ruta /error no queda habilitada como endpoint publico.
        assertEquals(401, enviar("GET", "/error", null, null).statusCode());
    }
}
