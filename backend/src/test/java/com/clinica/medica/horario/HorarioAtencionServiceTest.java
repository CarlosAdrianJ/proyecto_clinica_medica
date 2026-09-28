package com.clinica.medica.horario;

import com.clinica.medica.horario.dto.HorarioAtencionRequest;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.horario.service.HorarioAtencionService;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class HorarioAtencionServiceTest {

    private HorarioAtencionRepository horarioRepository;
    private ProfesionalRepository profesionalRepository;
    private HorarioAtencionService service;
    private Profesional profesional;
    private HorarioAtencion horario;

    @BeforeEach
    void preparar() {
        horarioRepository = mock(HorarioAtencionRepository.class);
        profesionalRepository = mock(ProfesionalRepository.class);

        service = new HorarioAtencionService(
                horarioRepository,
                profesionalRepository
        );

        profesional = new Profesional(
                "Ana",
                "Pérez",
                "MP1234",
                "3415550000",
                "ana@example.com"
        );
        profesional.setId(1L);

        horario = new HorarioAtencion();
        horario.setId(5L);
        horario.setProfesional(profesional);
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraDesde(LocalTime.of(8, 0));
        horario.setHoraHasta(LocalTime.of(12, 0));
        horario.setDuracionTurno(30);
        horario.setActivo(true);
    }

    @Test
    void listarHorariosActivos() {
        when(horarioRepository.findByActivoTrue())
                .thenReturn(List.of(horario));

        var resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals(DiaSemana.LUNES, resultado.getFirst().diaSemana());
        verify(horarioRepository).findByActivoTrue();
    }

    @Test
    void buscarHorarioPorId() {
        when(horarioRepository.findById(5L))
                .thenReturn(Optional.of(horario));

        var resultado = service.buscarPorId(5L);

        assertEquals(5L, resultado.id());
        assertEquals(1L, resultado.profesionalId());
    }

    @Test
    void buscarHorarioInexistenteDevuelve404() {
        when(horarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void crearHorarioCorrectamente() {
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(horarioRepository.contarSuperpuestos(
                1L,
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                null
        )).thenReturn(0L);

        when(horarioRepository.save(any(HorarioAtencion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = service.crear(requestValido());

        assertEquals(DiaSemana.LUNES, resultado.diaSemana());
        assertEquals(30, resultado.duracionTurno());
        assertTrue(resultado.activo());
        verify(horarioRepository).save(any(HorarioAtencion.class));
    }

    @Test
    void crearConProfesionalInexistenteDevuelve404() {
        when(profesionalRepository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(requestConProfesional(99L))
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void crearConProfesionalInactivoDevuelve409() {
        profesional.setActivo(false);

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(requestValido())
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void crearConHorasInvalidasDevuelve400() {
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        var request = new HorarioAtencionRequest(
                1L,
                DiaSemana.LUNES,
                LocalTime.of(12, 0),
                LocalTime.of(8, 0),
                30
        );

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void crearConDuracionMayorALaFranjaDevuelve400() {
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        var request = new HorarioAtencionRequest(
                1L,
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                90
        );

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void crearHorarioSuperpuestoDevuelve409() {
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(horarioRepository.contarSuperpuestos(
                1L,
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                null
        )).thenReturn(1L);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(requestValido())
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void eliminarRealizaBajaLogica() {
        when(horarioRepository.findById(5L))
                .thenReturn(Optional.of(horario));

        service.eliminar(5L);

        assertFalse(horario.getActivo());
        verify(horarioRepository).save(horario);
        verify(horarioRepository, never()).delete(any());
        verify(horarioRepository, never()).deleteById(anyLong());
    }

    private HorarioAtencionRequest requestValido() {
        return requestConProfesional(1L);
    }

    private HorarioAtencionRequest requestConProfesional(Long profesionalId) {
        return new HorarioAtencionRequest(
                profesionalId,
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                30
        );
    }
}