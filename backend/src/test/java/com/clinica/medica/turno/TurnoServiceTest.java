package com.clinica.medica.turno;

import com.clinica.medica.especialidad.model.Especialidad;
import com.clinica.medica.especialidad.repository.EspecialidadRepository;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.paciente.model.Paciente;
import com.clinica.medica.paciente.repository.PacienteRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.turno.dto.ReprogramarTurnoRequest;
import com.clinica.medica.turno.dto.TurnoRequest;
import com.clinica.medica.turno.model.EstadoTurno;
import com.clinica.medica.turno.model.Turno;
import com.clinica.medica.turno.repository.TurnoRepository;
import com.clinica.medica.turno.service.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class TurnoServiceTest {

    private TurnoRepository turnoRepository;
    private PacienteRepository pacienteRepository;
    private ProfesionalRepository profesionalRepository;
    private EspecialidadRepository especialidadRepository;
    private HorarioAtencionRepository horarioAtencionRepository;
    private TurnoService service;

    private Paciente paciente;
    private Profesional profesional;
    private Especialidad especialidad;
    private HorarioAtencion horario;
    private LocalDate fechaLunes;

    @BeforeEach
    void preparar() {
        turnoRepository = mock(TurnoRepository.class);
        pacienteRepository = mock(PacienteRepository.class);
        profesionalRepository = mock(ProfesionalRepository.class);
        especialidadRepository = mock(EspecialidadRepository.class);
        horarioAtencionRepository = mock(HorarioAtencionRepository.class);

        service = new TurnoService(
                turnoRepository,
                pacienteRepository,
                profesionalRepository,
                especialidadRepository,
                horarioAtencionRepository
        );

        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setNombre("Lucía");
        paciente.setApellido("Fernández");
        paciente.setActivo(true);

        especialidad = new Especialidad();
        especialidad.setId(2L);
        especialidad.setNombre("Clínica médica");
        especialidad.setActivo(true);

        profesional = new Profesional();
        profesional.setId(3L);
        profesional.setNombre("Laura");
        profesional.setApellido("Gómez");
        profesional.setActivo(true);
        profesional.getEspecialidades().add(especialidad);

        horario = new HorarioAtencion();
        horario.setProfesional(profesional);
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraDesde(LocalTime.of(8, 0));
        horario.setHoraHasta(LocalTime.of(12, 0));
        horario.setDuracionTurno(30);
        horario.setActivo(true);

        fechaLunes = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        when(turnoRepository.save(any(Turno.class)))
                .thenAnswer(invocacion -> {
                    Turno turno = invocacion.getArgument(0);
                    turno.prePersist();
                    return turno;
                });
    }

    @Test
    void crearTurnoValido() {
        prepararDatosValidos();
        when(turnoRepository.contarSuperpuestos(
                3L,
                fechaLunes,
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                EstadoTurno.CANCELADO,
                null
        )).thenReturn(0L);

        var resultado = service.crear(request(LocalTime.of(8, 0)));

        assertEquals(1L, resultado.pacienteId());
        assertEquals(3L, resultado.profesionalId());
        assertEquals(2L, resultado.especialidadId());
        assertEquals(LocalTime.of(8, 0), resultado.horaInicio());
        assertEquals(LocalTime.of(8, 30), resultado.horaFin());
        assertEquals(EstadoTurno.PENDIENTE, resultado.estado());
        assertEquals("Control general", resultado.observaciones());
        assertNotNull(resultado.fechaCreacion());
        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    void crearTurnoRecortaObservaciones() {
        prepararDatosValidos();
        when(turnoRepository.contarSuperpuestos(
                anyLong(),
                any(LocalDate.class),
                any(LocalTime.class),
                any(LocalTime.class),
                eq(EstadoTurno.CANCELADO),
                isNull()
        )).thenReturn(0L);

        TurnoRequest datos = new TurnoRequest(
                1L,
                3L,
                2L,
                fechaLunes,
                LocalTime.of(8, 0),
                "  Control general  "
        );

        var resultado = service.crear(datos);

        assertEquals("Control general", resultado.observaciones());
    }

    @Test
    void crearTurnoSuperpuestoDevuelve409() {
        prepararDatosValidos();
        when(turnoRepository.contarSuperpuestos(
                3L,
                fechaLunes,
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                EstadoTurno.CANCELADO,
                null
        )).thenReturn(1L);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request(LocalTime.of(8, 0)))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void crearPermiteReutilizarHorarioCancelado() {
        prepararDatosValidos();
        when(turnoRepository.contarSuperpuestos(
                3L,
                fechaLunes,
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                EstadoTurno.CANCELADO,
                null
        )).thenReturn(0L);

        var resultado = service.crear(request(LocalTime.of(8, 0)));

        assertEquals(EstadoTurno.PENDIENTE, resultado.estado());
        verify(turnoRepository).contarSuperpuestos(
                3L,
                fechaLunes,
                LocalTime.of(8, 0),
                LocalTime.of(8, 30),
                EstadoTurno.CANCELADO,
                null
        );
        verify(turnoRepository).save(any(Turno.class));
    }

    @Test
    void crearFueraDelHorarioDevuelve409() {
        prepararDatosValidos();

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request(LocalTime.of(12, 0)))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void crearConEspecialidadNoAsociadaDevuelve409() {
        when(pacienteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(paciente));
        when(profesionalRepository.findById(3L))
                .thenReturn(Optional.of(profesional));
        when(especialidadRepository.findByIdAndActivoTrue(4L))
                .thenReturn(Optional.of(especialidad));

        TurnoRequest datos = new TurnoRequest(
                1L,
                3L,
                4L,
                fechaLunes,
                LocalTime.of(8, 0),
                null
        );

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(datos)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void crearConFechaPasadaDevuelve409() {
        TurnoRequest datos = new TurnoRequest(
                1L,
                3L,
                2L,
                LocalDate.now().minusDays(1),
                LocalTime.of(8, 0),
                null
        );

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(datos)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verifyNoInteractions(
                pacienteRepository,
                profesionalRepository,
                especialidadRepository,
                horarioAtencionRepository
        );
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void cancelarTurnoPendiente() {
        Turno turno = turnoPendiente();
        when(turnoRepository.findById(10L))
                .thenReturn(Optional.of(turno));

        var resultado = service.cancelar(10L);

        assertEquals(EstadoTurno.CANCELADO, resultado.estado());
        verify(turnoRepository).save(turno);
    }

    @Test
    void noPermiteCambiarUnTurnoEnEstadoFinal() {
        Turno turno = turnoPendiente();
        turno.setEstado(EstadoTurno.ATENDIDO);
        when(turnoRepository.findById(10L))
                .thenReturn(Optional.of(turno));

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.cancelar(10L)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void reprogramarTurnoPendiente() {
        Turno turno = turnoPendiente();
        when(turnoRepository.findById(10L))
                .thenReturn(Optional.of(turno));
        when(horarioAtencionRepository.findByProfesionalIdAndActivoTrue(3L))
                .thenReturn(List.of(horario));
        when(turnoRepository.contarSuperpuestos(
                3L,
                fechaLunes,
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                EstadoTurno.CANCELADO,
                null
        )).thenReturn(0L);

        var resultado = service.reprogramar(
                10L,
                new ReprogramarTurnoRequest(
                        fechaLunes,
                        LocalTime.of(9, 0)
                )
        );

        assertEquals(LocalTime.of(9, 0), resultado.horaInicio());
        assertEquals(LocalTime.of(9, 30), resultado.horaFin());
        assertEquals(EstadoTurno.PENDIENTE, resultado.estado());
        verify(turnoRepository).save(turno);
    }

    @Test
    void noPermiteReprogramarTurnoCancelado() {
        Turno turno = turnoPendiente();
        turno.setEstado(EstadoTurno.CANCELADO);
        when(turnoRepository.findById(10L))
                .thenReturn(Optional.of(turno));

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.reprogramar(
                        10L,
                        new ReprogramarTurnoRequest(
                                fechaLunes,
                                LocalTime.of(9, 0)
                        )
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void buscarTurnoInexistenteDevuelve404() {
        when(turnoRepository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    private void prepararDatosValidos() {
        when(pacienteRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(paciente));
        when(profesionalRepository.findById(3L))
                .thenReturn(Optional.of(profesional));
        when(especialidadRepository.findByIdAndActivoTrue(2L))
                .thenReturn(Optional.of(especialidad));
        when(horarioAtencionRepository.findByProfesionalIdAndActivoTrue(3L))
                .thenReturn(List.of(horario));
    }

    private TurnoRequest request(LocalTime horaInicio) {
        return new TurnoRequest(
                1L,
                3L,
                2L,
                fechaLunes,
                horaInicio,
                "Control general"
        );
    }

    private Turno turnoPendiente() {
        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setEspecialidad(especialidad);
        turno.setFecha(fechaLunes);
        turno.setHoraInicio(LocalTime.of(8, 0));
        turno.setHoraFin(LocalTime.of(8, 30));
        turno.setEstado(EstadoTurno.PENDIENTE);
        turno.setObservaciones("Control general");
        turno.prePersist();
        return turno;
    }
}