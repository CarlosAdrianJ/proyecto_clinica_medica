package com.clinica.medica.disponibilidad;

import com.clinica.medica.disponibilidad.service.DisponibilidadService;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DisponibilidadServiceTest {

    private ProfesionalRepository profesionalRepository;
    private HorarioAtencionRepository horarioAtencionRepository;
    private DisponibilidadService service;
    private Profesional profesional;

    @BeforeEach
    void preparar() {
        profesionalRepository = mock(ProfesionalRepository.class);
        horarioAtencionRepository = mock(HorarioAtencionRepository.class);

        service = new DisponibilidadService(
                profesionalRepository,
                horarioAtencionRepository
        );

        profesional = new Profesional();
        profesional.setId(1L);
        profesional.setNombre("Ana");
        profesional.setApellido("Pérez");
        profesional.setMatricula("MP-100");
        profesional.setActivo(true);
    }

    @Test
    void generaFranjasDisponiblesParaElDiaSolicitado() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                30
        );

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(horarioAtencionRepository
                .findByProfesionalIdAndActivoTrue(1L))
                .thenReturn(List.of(horario));

        var resultado = service.consultar(
                1L,
                LocalDate.of(2026, 10, 5)
        );

        assertEquals(1L, resultado.profesionalId());
        assertEquals(LocalDate.of(2026, 10, 5), resultado.fecha());
        assertEquals(4, resultado.franjas().size());
        assertEquals(
                LocalTime.of(8, 0),
                resultado.franjas().getFirst().horaInicio()
        );
        assertEquals(
                LocalTime.of(10, 0),
                resultado.franjas().getLast().horaFin()
        );
    }

    @Test
    void devuelveListaVaciaCuandoNoAtiendeEseDia() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.MARTES,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                30
        );

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(horarioAtencionRepository
                .findByProfesionalIdAndActivoTrue(1L))
                .thenReturn(List.of(horario));

        var resultado = service.consultar(
                1L,
                LocalDate.of(2026, 10, 5)
        );

        assertTrue(resultado.franjas().isEmpty());
    }

    @Test
    void profesionalInexistenteDevuelve404() {
        when(profesionalRepository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        99L,
                        LocalDate.of(2026, 10, 5)
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(horarioAtencionRepository);
    }

    @Test
    void profesionalInactivoDevuelve409() {
        profesional.setActivo(false);

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(
                        1L,
                        LocalDate.of(2026, 10, 5)
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verifyNoInteractions(horarioAtencionRepository);
    }

    @Test
    void noGeneraFranjaQueSupereLaHoraHasta() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(9, 10),
                30
        );

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(horarioAtencionRepository
                .findByProfesionalIdAndActivoTrue(1L))
                .thenReturn(List.of(horario));

        var resultado = service.consultar(
                1L,
                LocalDate.of(2026, 10, 5)
        );

        assertEquals(2, resultado.franjas().size());
        assertEquals(
                LocalTime.of(9, 0),
                resultado.franjas().getLast().horaFin()
        );
    }

    @Test
    void ordenaLosBloquesPorHoraDeInicio() {
        HorarioAtencion horarioTarde = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                30
        );

        HorarioAtencion horarioManana = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                30
        );

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(horarioAtencionRepository
                .findByProfesionalIdAndActivoTrue(1L))
                .thenReturn(List.of(horarioTarde, horarioManana));

        var resultado = service.consultar(
                1L,
                LocalDate.of(2026, 10, 5)
        );

        assertEquals(
                LocalTime.of(8, 0),
                resultado.franjas().getFirst().horaInicio()
        );
        assertEquals(
                LocalTime.of(15, 0),
                resultado.franjas().getLast().horaFin()
        );
    }

    private HorarioAtencion crearHorario(
            DiaSemana diaSemana,
            LocalTime horaDesde,
            LocalTime horaHasta,
            int duracionTurno
    ) {
        HorarioAtencion horario = new HorarioAtencion();
        horario.setProfesional(profesional);
        horario.setDiaSemana(diaSemana);
        horario.setHoraDesde(horaDesde);
        horario.setHoraHasta(horaHasta);
        horario.setDuracionTurno(duracionTurno);
        horario.setActivo(true);
        return horario;
    }
}