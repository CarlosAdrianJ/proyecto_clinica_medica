package com.clinica.medica.turno.service;

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
import com.clinica.medica.turno.dto.TurnoResponse;
import com.clinica.medica.turno.model.EstadoTurno;
import com.clinica.medica.turno.model.Turno;
import com.clinica.medica.turno.repository.TurnoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EspecialidadRepository especialidadRepository;
    private final HorarioAtencionRepository horarioAtencionRepository;

    public TurnoService(
            TurnoRepository turnoRepository,
            PacienteRepository pacienteRepository,
            ProfesionalRepository profesionalRepository,
            EspecialidadRepository especialidadRepository,
            HorarioAtencionRepository horarioAtencionRepository
    ) {
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
        this.profesionalRepository = profesionalRepository;
        this.especialidadRepository = especialidadRepository;
        this.horarioAtencionRepository = horarioAtencionRepository;
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> listar() {
        return turnoRepository.findAllByOrderByFechaAscHoraInicioAsc()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TurnoResponse> buscar(
            Long pacienteId,
            Long profesionalId,
            LocalDate fecha,
            EstadoTurno estado
    ) {
        return turnoRepository.buscar(
                        pacienteId,
                        profesionalId,
                        fecha,
                        estado
                )
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TurnoResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerTurno(id));
    }

    @Transactional
    public TurnoResponse crear(TurnoRequest request) {
        validarFecha(request.fecha());

        Paciente paciente = pacienteRepository
                .findByIdAndActivoTrue(request.pacienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paciente no encontrado o inactivo"
                ));

        Profesional profesional = obtenerProfesionalActivo(
                request.profesionalId()
        );

        Especialidad especialidad = especialidadRepository
                .findByIdAndActivoTrue(request.especialidadId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Especialidad no encontrada o inactiva"
                ));

        validarAsociacion(profesional, especialidad.getId());

        LocalTime horaFin = validarHorarioYCalcularFin(
                profesional.getId(),
                request.fecha(),
                request.horaInicio()
        );

        validarSuperposicion(
                profesional.getId(),
                request.fecha(),
                request.horaInicio(),
                horaFin,
                null
        );

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setProfesional(profesional);
        turno.setEspecialidad(especialidad);
        turno.setFecha(request.fecha());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(horaFin);
        turno.setEstado(EstadoTurno.PENDIENTE);
        turno.setObservaciones(limpiarObservaciones(request.observaciones()));

        return convertirAResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse reprogramar(
            Long id,
            ReprogramarTurnoRequest request
    ) {
        Turno turno = obtenerTurno(id);

        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo pueden reprogramarse turnos pendientes"
            );
        }

        validarFecha(request.fecha());

        LocalTime horaFin = validarHorarioYCalcularFin(
                turno.getProfesional().getId(),
                request.fecha(),
                request.horaInicio()
        );

        validarSuperposicion(
                turno.getProfesional().getId(),
                request.fecha(),
                request.horaInicio(),
                horaFin,
                turno.getId()
        );

        turno.setFecha(request.fecha());
        turno.setHoraInicio(request.horaInicio());
        turno.setHoraFin(horaFin);

        return convertirAResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cambiarEstado(
            Long id,
            EstadoTurno nuevoEstado
    ) {
        Turno turno = obtenerTurno(id);

        if (turno.getEstado() != EstadoTurno.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El turno se encuentra en un estado final"
            );
        }

        if (nuevoEstado == EstadoTurno.PENDIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El turno ya se encuentra pendiente"
            );
        }

        turno.setEstado(nuevoEstado);

        return convertirAResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cancelar(Long id) {
        return cambiarEstado(id, EstadoTurno.CANCELADO);
    }

    private Turno obtenerTurno(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Turno no encontrado"
                ));
    }

    private Profesional obtenerProfesionalActivo(Long id) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profesional no encontrado"
                ));

        if (!Boolean.TRUE.equals(profesional.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El profesional se encuentra inactivo"
            );
        }

        return profesional;
    }

    private void validarAsociacion(
            Profesional profesional,
            Long especialidadId
    ) {
        boolean asociada = profesional.getEspecialidades()
                .stream()
                .anyMatch(especialidad ->
                        especialidad.getId().equals(especialidadId)
                );

        if (!asociada) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La especialidad no está habilitada para el profesional"
            );
        }
    }

    private LocalTime validarHorarioYCalcularFin(
            Long profesionalId,
            LocalDate fecha,
            LocalTime horaInicio
    ) {
        DiaSemana diaSemana = convertirDiaSemana(fecha.getDayOfWeek());

        return horarioAtencionRepository
                .findByProfesionalIdAndActivoTrue(profesionalId)
                .stream()
                .filter(horario ->
                        horario.getDiaSemana() == diaSemana
                )
                .filter(horario ->
                        esInicioValido(horario, horaInicio)
                )
                .findFirst()
                .map(horario ->
                        horaInicio.plusMinutes(
                                horario.getDuracionTurno()
                        )
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El horario solicitado no se encuentra disponible"
                ));
    }

    private boolean esInicioValido(
            HorarioAtencion horario,
            LocalTime horaInicio
    ) {
        LocalTime horaFin = horaInicio.plusMinutes(
                horario.getDuracionTurno()
        );

        if (horaInicio.isBefore(horario.getHoraDesde())
                || horaFin.isAfter(horario.getHoraHasta())) {
            return false;
        }

        long minutosDesdeInicio = ChronoUnit.MINUTES.between(
                horario.getHoraDesde(),
                horaInicio
        );

        return minutosDesdeInicio % horario.getDuracionTurno() == 0;
    }

    private void validarSuperposicion(
            Long profesionalId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long turnoId
    ) {
        long superpuestos = turnoRepository.contarSuperpuestos(
                profesionalId,
                fecha,
                horaInicio,
                horaFin,
                EstadoTurno.CANCELADO,
                turnoId
        );

        if (superpuestos > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El profesional ya tiene un turno en esa franja horaria"
            );
        }
    }

    private void validarFecha(LocalDate fecha) {
        if (fecha.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La fecha del turno no puede ser anterior a la actual"
            );
        }
    }

    private String limpiarObservaciones(String observaciones) {
        if (observaciones == null || observaciones.isBlank()) {
            return null;
        }

        return observaciones.trim();
    }

    private DiaSemana convertirDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private TurnoResponse convertirAResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getPaciente().getId(),
                turno.getPaciente().getNombre()
                        + " "
                        + turno.getPaciente().getApellido(),
                turno.getProfesional().getId(),
                turno.getProfesional().getNombre()
                        + " "
                        + turno.getProfesional().getApellido(),
                turno.getEspecialidad().getId(),
                turno.getEspecialidad().getNombre(),
                turno.getFecha(),
                turno.getHoraInicio(),
                turno.getHoraFin(),
                turno.getEstado(),
                turno.getObservaciones(),
                turno.getFechaCreacion()
        );
    }
}