package com.clinica.medica.security;

import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.usuario.model.*;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class SeguridadIntegrationTest {
    @Autowired
    WebApplicationContext context;
    @Autowired
    UsuarioRepository usuarios;
    @Autowired
    ProfesionalRepository profesionales;
    @Autowired
    PasswordEncoder passwords;
    @Autowired
    JwtEncoder encoder;
    MockMvc mvc;
    Profesional propio;
    Profesional ajeno;
    static String hash;

    @BeforeAll
    static void hash() {
        hash = new BCryptPasswordEncoder(12).encode("Prueba123!");
    }

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        propio = profesionales.saveAndFlush(new Profesional("Ana", "Perez", "MP1", null, null));
        ajeno = profesionales.saveAndFlush(new Profesional("Eva", "Lopez", "MP2", null, null));
        crear("admin", Rol.ADMINISTRADOR, null);
        crear("recepcion", Rol.RECEPCIONISTA, null);
        crear("profesional", Rol.PROFESIONAL, propio);
    }

    Usuario crear(String nombre, Rol rol, Profesional p) {
        Usuario u = new Usuario();
        u.setNombreUsuario(nombre);
        u.setContrasenaHash(hash);
        u.setRol(rol);
        u.setProfesional(p);
        return usuarios.saveAndFlush(u);
    }

    String login(String nombre) throws Exception {
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"" + nombre + "\",\"contrasena\":\"Prueba123!\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.contrasenaHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    @Test
    void loginEmiteJwtYPermiteConsultarIdentidad() throws Exception {
        String token = login("ADMIN");
        assertEquals(3, token.split("\\.").length);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombreUsuario").value("admin"));
    }

    @ParameterizedTest
    @CsvSource({ "admin,incorrecta", "inexistente,Prueba123!" })
    void credencialesInvalidas(String nombre, String password) throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"" + nombre + "\",\"contrasena\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenOTokenMalformadoEs401() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
            "admin,/api/usuarios,200", "recepcion,/api/usuarios,403", "profesional,/api/usuarios,403",
            "admin,/api/pacientes,200", "recepcion,/api/pacientes,200", "profesional,/api/pacientes,403",
            "admin,/api/profesionales,200", "recepcion,/api/profesionales,200", "profesional,/api/profesionales,403",
            "profesional,/api/horarios-atencion,403", "profesional,/api/especialidades,200" })
    void matrizDeLectura(String nombre, String url, int esperado) throws Exception {
        mvc.perform(get(url).header("Authorization", "Bearer " + login(nombre))).andExpect(status().is(esperado));
    }

    @Test
    void profesionalSoloConsultaSusDatosHorariosYDisponibilidad() throws Exception {
        String token = login("profesional");
        for (String ruta : new String[] { "/api/profesionales/", "/api/horarios-atencion/profesional/" }) {
            mvc.perform(get(ruta + propio.getId()).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            mvc.perform(get(ruta + ajeno.getId()).header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/disponibilidad").param("profesionalId", propio.getId().toString())
                .param("fecha", "2026-10-19")
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(
                get("/api/disponibilidad").param("profesionalId", ajeno.getId().toString()).param("fecha", "2026-10-19")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/profesionales/matricula/MP2").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @CsvSource({ "recepcion", "profesional" })
    void soloAdminPuedeModificarCatalogos(String nombre) throws Exception {
        String token = login(nombre);
        for (String url : new String[] { "/api/profesionales", "/api/especialidades", "/api/horarios-atencion",
                "/api/usuarios" }) {
            mvc.perform(post(url).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isForbidden());
            mvc.perform(put(url + "/1").header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
            mvc.perform(delete(url + "/1").header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    String cuerpo(String nombre, String rol, String profesionalId) {
        return "{\"nombreUsuario\":\"" + nombre + "\",\"contrasena\":\"Nueva123!\",\"rol\":\"" + rol +
                "\",\"activo\":true,\"profesionalId\":" + profesionalId + "}";
    }

    @Test
    void adminCreaActualizaYDesactivaUsuarioConBCrypt() throws Exception {
        String token = login("admin");
        String json = mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo("nuevo", "RECEPCIONISTA", "null")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.contrasenaHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(json, "$.id");
        Usuario u = usuarios.findById(id.longValue()).orElseThrow();
        assertTrue(u.getContrasenaHash().startsWith("$2a$12$"));
        assertTrue(passwords.matches("Nueva123!", u.getContrasenaHash()));
        mvc.perform(put("/api/usuarios/" + id).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("nuevo", "PROFESIONAL", propio.getId().toString())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.profesionalId").value(propio.getId()));
        mvc.perform(delete("/api/usuarios/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertFalse(u.isActivo());
    }

    @Test
    void duplicadosEInvariantesDeAsociacion() throws Exception {
        String token = login("admin");
        mvc.perform(
                post("/api/usuarios").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ADMIN", "ADMINISTRADOR", "null")))
                .andExpect(status().isConflict());
        for (String json : new String[] { cuerpo("nuevo", "PROFESIONAL", "null"),
                cuerpo("nuevo", "PROFESIONAL", "999999"),
                cuerpo("nuevo", "RECEPCIONISTA", propio.getId().toString()), "{}" })
            mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)).andExpect(status().isBadRequest());
        propio.setActivo(false);
        profesionales.flush();
        mvc.perform(
                post("/api/usuarios").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("nuevo", "PROFESIONAL", propio.getId().toString())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bajaDeCuentaRevocaTokenYLogin() throws Exception {
        String token = login("recepcion");
        usuarios.findByNombreUsuario("recepcion").orElseThrow().setActivo(false);
        usuarios.flush();
        mvc.perform(get("/api/pacientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"recepcion\",\"contrasena\":\"Prueba123!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bajaProfesionalImpideUsarSuToken() throws Exception {
        String token = login("profesional");
        propio.setActivo(false);
        profesionales.flush();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cambioDeRolSeAplicaAlTokenExistente() throws Exception {
        String token = login("admin");
        usuarios.findByNombreUsuario("admin").orElseThrow().setRol(Rol.RECEPCIONISTA);
        usuarios.flush();
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void jwtVencidoFirmaAlteradaYEmisorAjenoSeRechazan() throws Exception {
        Usuario u = usuarios.findByNombreUsuario("admin").orElseThrow();
        for (String issuer : new String[] { "clinica-medica", "ajeno" }) {
            Instant ahora = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject("admin")
                    .claim("uid", u.getId().toString())
                    .issuedAt(ahora.minusSeconds(600))
                    .expiresAt(issuer.equals("ajeno") ? ahora.plusSeconds(3600) : ahora.minusSeconds(300)).build();
            String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                    .getTokenValue();
            mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
        String[] partes = login("admin").split("\\.");
        partes[2] = (partes[2].startsWith("A") ? "B" : "A") + partes[2].substring(1);
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + String.join(".", partes)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validacionesNoExponenContrasenaYRespetanLimiteUtf8DeBCrypt() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombreUsuario\":\"admin\",\"contrasena\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());
        String token = login("admin");
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("nuevo", "RECEPCIONISTA", "null").replace("Nueva123!", "corta")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("corta"))));
        String larga = "\u00e9".repeat(40); // 40 caracteres, 80 bytes UTF-8.
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo("nuevo", "RECEPCIONISTA", "null").replace("Nueva123!", larga)))
                .andExpect(status().isBadRequest());
        assertTrue(usuarios.findByNombreUsuario("nuevo").isEmpty());
    }

    @Test
    void profesionalPuedeConsultarSolamenteSuAgenda() throws Exception {
        String token = login("profesional");

        mvc.perform(get("/api/turnos/mi-agenda")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mvc.perform(get("/api/turnos/mi-agenda")
                .param("fecha", "2026-10-19")
                .param("estado", "PENDIENTE")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mvc.perform(get("/api/turnos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorYRecepcionistaNoUsanLaAgendaProfesional() throws Exception {
        for (String usuario : new String[] { "admin", "recepcion" }) {
            String token = login(usuario);

            mvc.perform(get("/api/turnos/mi-agenda")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());

            mvc.perform(get("/api/turnos")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }
}
