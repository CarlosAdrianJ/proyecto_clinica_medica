package com.clinica.medica.disponibilidad;

import com.clinica.medica.disponibilidad.service.DisponibilidadService;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.turno.model.EstadoTurno;
import com.clinica.medica.turno.model.Turno;
import com.clinica.medica.turno.repository.TurnoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class DisponibilidadServiceTest {

    private ProfesionalRepository profesionalRepository;
    private HorarioAtencionRepository horarioAtencionRepository;
    private TurnoRepository turnoRepository;
    private DisponibilidadService service;
    private Profesional profesional;
    private LocalDate fechaConsulta;

    @BeforeEach
    void preparar() {
        profesionalRepository = mock(ProfesionalRepository.class);
        horarioAtencionRepository = mock(HorarioAtencionRepository.class);
        turnoRepository = mock(TurnoRepository.class);

        service = new DisponibilidadService(
                profesionalRepository,
                horarioAtencionRepository,
                turnoRepository
        );

        profesional = new Profesional();
        profesional.setId(1L);
        profesional.setNombre("Ana");
        profesional.setApellido("Pérez");
        profesional.setMatricula("MP-100");
        profesional.setActivo(true);

        fechaConsulta = LocalDate.of(2026, 10, 5);

        when(turnoRepository
                .findByProfesionalIdAndFechaAndEstadoNotOrderByHoraInicioAsc(
                        anyLong(),
                        any(LocalDate.class),
                        eq(EstadoTurno.CANCELADO)
                ))
                .thenReturn(List.of());
    }

    @Test
    void generaFranjasDisponiblesParaElDiaSolicitado() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                30
        );

        prepararProfesionalYHorarios(List.of(horario));

        var resultado = service.consultar(1L, fechaConsulta);

        assertEquals(1L, resultado.profesionalId());
        assertEquals(fechaConsulta, resultado.fecha());
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

        prepararProfesionalYHorarios(List.of(horario));

        var resultado = service.consultar(1L, fechaConsulta);

        assertTrue(resultado.franjas().isEmpty());
    }

    @Test
    void profesionalInexistenteDevuelve404() {
        when(profesionalRepository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(99L, fechaConsulta)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(horarioAtencionRepository);
        verifyNoInteractions(turnoRepository);
    }

    @Test
    void profesionalInactivoDevuelve409() {
        profesional.setActivo(false);

        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.consultar(1L, fechaConsulta)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verifyNoInteractions(horarioAtencionRepository);
        verifyNoInteractions(turnoRepository);
    }

    @Test
    void noGeneraFranjaQueSupereLaHoraHasta() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(9, 10),
                30
        );

        prepararProfesionalYHorarios(List.of(horario));

        var resultado = service.consultar(1L, fechaConsulta);

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

        prepararProfesionalYHorarios(
                List.of(horarioTarde, horarioManana)
        );

        var resultado = service.consultar(1L, fechaConsulta);

        assertEquals(
                LocalTime.of(8, 0),
                resultado.franjas().getFirst().horaInicio()
        );
        assertEquals(
                LocalTime.of(15, 0),
                resultado.franjas().getLast().horaFin()
        );
    }

    @Test
    void excluyeFranjaOcupadaPorUnTurno() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                30
        );

        Turno turnoOcupado = crearTurno(
                LocalTime.of(8, 30),
                LocalTime.of(9, 0),
                EstadoTurno.PENDIENTE
        );

        prepararProfesionalYHorarios(List.of(horario));
        when(turnoRepository
                .findByProfesionalIdAndFechaAndEstadoNotOrderByHoraInicioAsc(
                        1L,
                        fechaConsulta,
                        EstadoTurno.CANCELADO
                ))
                .thenReturn(List.of(turnoOcupado));

        var resultado = service.consultar(1L, fechaConsulta);

        assertEquals(3, resultado.franjas().size());
        assertFalse(resultado.franjas().stream().anyMatch(
                franja -> franja.horaInicio().equals(LocalTime.of(8, 30))
        ));
    }

    @Test
    void consultaTurnosExcluyendoLosCancelados() {
        HorarioAtencion horario = crearHorario(
                DiaSemana.LUNES,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                30
        );

        prepararProfesionalYHorarios(List.of(horario));

        var resultado = service.consultar(1L, fechaConsulta);

        assertEquals(2, resultado.franjas().size());
        verify(turnoRepository)
                .findByProfesionalIdAndFechaAndEstadoNotOrderByHoraInicioAsc(
                        1L,
                        fechaConsulta,
                        EstadoTurno.CANCELADO
                );
    }

    private void prepararProfesionalYHorarios(
            List<HorarioAtencion> horarios
    ) {
        when(profesionalRepository.findById(1L))
                .thenReturn(Optional.of(profesional));
        when(horarioAtencionRepository
                .findByProfesionalIdAndActivoTrue(1L))
                .thenReturn(horarios);
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

    private Turno crearTurno(
            LocalTime horaInicio,
            LocalTime horaFin,
            EstadoTurno estado
    ) {
        Turno turno = new Turno();
        turno.setProfesional(profesional);
        turno.setFecha(fechaConsulta);
        turno.setHoraInicio(horaInicio);
        turno.setHoraFin(horaFin);
        turno.setEstado(estado);
        return turno;
    }
}