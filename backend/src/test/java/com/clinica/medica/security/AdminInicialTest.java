package com.clinica.medica.security;

import com.clinica.medica.usuario.model.*;
import com.clinica.medica.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminInicialTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Test void sinConfiguracionNoCreaUsuarios() {
        new AdminInicial(usuarios, encoder, "", "").run();
        verifyNoInteractions(usuarios);
    }
    @Test void creaAdministradorConBCryptSiLaTablaEstaVacia() {
        new AdminInicial(usuarios, encoder, "ADMIN", "Inicial123!").run();
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario admin = captor.getValue();
        assertEquals("admin", admin.getNombreUsuario());
        assertEquals(Rol.ADMINISTRADOR, admin.getRol());
        assertTrue(admin.isActivo());
        assertNull(admin.getProfesional());
        assertTrue(encoder.matches("Inicial123!", admin.getContrasenaHash()));
    }
    @Test void noSobrescribeCuentasExistentes() {
        when(usuarios.count()).thenReturn(1L);
        new AdminInicial(usuarios, encoder, "admin", "Inicial123!").run();
        verify(usuarios, never()).save(any());
    }
    @Test void rechazaConfiguracionParcialOInvalida() {
        for (String[] credenciales : new String[][]{{"admin", ""}, {"", "Inicial123!"}, {"admin", "corta"}}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new AdminInicial(usuarios, encoder, credenciales[0], credenciales[1]).run());
        }
        verify(usuarios, never()).save(any());
    }
}
