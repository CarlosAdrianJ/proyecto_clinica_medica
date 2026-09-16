package com.clinica.medica.paciente;

import com.clinica.medica.paciente.dto.PacienteRequest;
import com.clinica.medica.paciente.model.Paciente;
import com.clinica.medica.paciente.repository.PacienteRepository;
import com.clinica.medica.paciente.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class PacienteServiceTest {

    private PacienteRepository repository;
    private PacienteService service;
    private Paciente paciente;

    @BeforeEach
    void preparar() {
        repository = mock(PacienteRepository.class);
        service = new PacienteService(repository);

        paciente = new Paciente();
        paciente.setNombre("Lucía");
        paciente.setApellido("Fernández");
        paciente.setDni("40123456");
        paciente.setFechaNacimiento(LocalDate.of(1995, 6, 18));
    }

    @Test
    void listarPacientesActivos() {
        when(repository.findAllByActivoTrue()).thenReturn(List.of(paciente));

        var resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("40123456", resultado.getFirst().dni());
        verify(repository).findAllByActivoTrue();
    }

    @Test
    void listarSinPacientes() {
        when(repository.findAllByActivoTrue()).thenReturn(List.of());

        assertTrue(service.listar().isEmpty());
    }

    @Test
    void buscarPacienteActivo() {
        when(repository.findByIdAndActivoTrue(4L))
                .thenReturn(Optional.of(paciente));

        var resultado = service.buscarPorId(4L);

        assertEquals("Lucía", resultado.nombre());
        assertEquals("40123456", resultado.dni());
    }

    @Test
    void buscarPacienteInexistenteDevuelve404() {
        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void crearPaciente() {
        when(repository.save(any(Paciente.class)))
        .thenAnswer(invocacion -> {
            Paciente nuevo = invocacion.getArgument(0);
            nuevo.prePersist();
            return nuevo;
        });
        var resultado = service.crear(request("40123456"));
        assertEquals("Lucía", resultado.nombre());
        assertEquals("Fernández", resultado.apellido());
        assertEquals("40123456", resultado.dni());
        assertTrue(resultado.activo());
        assertNotNull(resultado.fechaAlta());
        verify(repository).save(any(Paciente.class));
    }

    @Test
    void crearConDniDuplicadoDevuelve409() {
        when(repository.existsByDni("40123456")).thenReturn(true);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request("40123456"))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(Paciente.class));
    }

    @Test
    void actualizarPaciente() {
        when(repository.findByIdAndActivoTrue(4L))
                .thenReturn(Optional.of(paciente));
        when(repository.save(any(Paciente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = service.actualizar(4L, request("40222333"));

        assertEquals("40222333", resultado.dni());
        assertEquals("3415557788", resultado.telefono());
        verify(repository).existsByDniAndIdNot("40222333", 4L);
        verify(repository).save(paciente);
    }

    @Test
    void actualizarConDniDuplicadoDevuelve409() {
        when(repository.findByIdAndActivoTrue(4L))
                .thenReturn(Optional.of(paciente));
        when(repository.existsByDniAndIdNot("40222333", 4L))
                .thenReturn(true);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(4L, request("40222333"))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("40123456", paciente.getDni());
        verify(repository, never()).save(any(Paciente.class));
    }

    @Test
    void actualizarPacienteInexistenteDevuelve404() {
        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(99L, request("40123456"))
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).save(any(Paciente.class));
    }

    @Test
    void eliminarRealizaBajaLogica() {
        when(repository.findByIdAndActivoTrue(4L))
                .thenReturn(Optional.of(paciente));

        service.eliminar(4L);

        assertFalse(paciente.isActivo());
        verify(repository).save(paciente);
        verify(repository, never()).delete(any(Paciente.class));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarPacienteInexistenteDevuelve404() {
        when(repository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).save(any(Paciente.class));
    }

    @Test
    void crearRecortaEspaciosEnNombreApellidoYDni() {
        when(repository.save(any(Paciente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var datos = new PacienteRequest(
                " Lucía ",
                " Fernández ",
                " 40123456 ",
                LocalDate.of(1995, 6, 18),
                null,
                null,
                null
        );

        var resultado = service.crear(datos);

        assertEquals("Lucía", resultado.nombre());
        assertEquals("Fernández", resultado.apellido());
        assertEquals("40123456", resultado.dni());
    }

    private PacienteRequest request(String dni) {
        return new PacienteRequest(
                "Lucía",
                "Fernández",
                dni,
                LocalDate.of(1995, 6, 18),
                "3415557788",
                "lucia.fernandez@example.com",
                "Mendoza 1450"
        );
    }
}