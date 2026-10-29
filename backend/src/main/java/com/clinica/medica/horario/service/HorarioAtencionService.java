package com.clinica.medica.horario.service;

import com.clinica.medica.horario.dto.HorarioAtencionRequest;
import com.clinica.medica.horario.dto.HorarioAtencionResponse;
import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import com.clinica.medica.horario.repository.HorarioAtencionRepository;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HorarioAtencionService {

    private static final LocalTime HORA_APERTURA =
            LocalTime.of(8, 0);

    private static final LocalTime CIERRE_LUNES_A_VIERNES =
            LocalTime.of(20, 0);

    private static final LocalTime CIERRE_SABADO =
            LocalTime.of(12, 0);

    private final HorarioAtencionRepository horarioRepository;
    private final ProfesionalRepository profesionalRepository;

    public HorarioAtencionService(
            HorarioAtencionRepository horarioRepository,
            ProfesionalRepository profesionalRepository
    ) {
        this.horarioRepository = horarioRepository;
        this.profesionalRepository = profesionalRepository;
    }

    public List<HorarioAtencionResponse> listar() {
        return horarioRepository.findByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public HorarioAtencionResponse buscarPorId(Long id) {
        return convertirAResponse(obtenerHorario(id));
    }

    public List<HorarioAtencionResponse> listarPorProfesional(
            Long profesionalId
    ) {
        obtenerProfesionalActivo(profesionalId);

        return horarioRepository
                .findByProfesionalIdAndActivoTrue(profesionalId)
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional
    public HorarioAtencionResponse crear(
            HorarioAtencionRequest request
    ) {
        Profesional profesional =
                obtenerProfesionalActivo(request.profesionalId());

        validarHorario(request, null);

        HorarioAtencion horario = new HorarioAtencion();
        actualizarDatos(horario, profesional, request);

        return convertirAResponse(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioAtencionResponse actualizar(
            Long id,
            HorarioAtencionRequest request
    ) {
        HorarioAtencion horario = obtenerHorario(id);
        Profesional profesional =
                obtenerProfesionalActivo(request.profesionalId());

        validarHorario(request, id);
        actualizarDatos(horario, profesional, request);

        return convertirAResponse(horarioRepository.save(horario));
    }

    @Transactional
    public void eliminar(Long id) {
        HorarioAtencion horario = obtenerHorario(id);
        horario.setActivo(false);
        horarioRepository.save(horario);
    }

    private HorarioAtencion obtenerHorario(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Horario de atención no encontrado"
                ));
    }

    private Profesional obtenerProfesionalActivo(Long id) {
        Profesional profesional = profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profesional no encontrado"
                ));

        if (Boolean.FALSE.equals(profesional.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El profesional se encuentra inactivo"
            );
        }

        return profesional;
    }

    private void validarHorario(
            HorarioAtencionRequest request,
            Long horarioId
    ) {
        if (!request.horaDesde().isBefore(request.horaHasta())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora de inicio debe ser anterior a la hora final"
            );
        }

        if (request.diaSemana() == DiaSemana.DOMINGO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La clínica no atiende los domingos"
            );
        }

        LocalTime horaCierre = obtenerHoraCierre(request.diaSemana());

        if (request.horaDesde().isBefore(HORA_APERTURA)
                || request.horaHasta().isAfter(horaCierre)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El horario debe estar dentro de la franja de atención de la clínica"
            );
        }

        long minutosDisponibles = Duration.between(
                request.horaDesde(),
                request.horaHasta()
        ).toMinutes();

        if (request.duracionTurno() > minutosDisponibles) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La duración no puede superar la franja horaria"
            );
        }

        long superpuestos = horarioRepository.contarSuperpuestos(
                request.profesionalId(),
                request.diaSemana(),
                request.horaDesde(),
                request.horaHasta(),
                horarioId
        );

        if (superpuestos > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El horario se superpone con otro horario activo"
            );
        }
    }

    private LocalTime obtenerHoraCierre(DiaSemana diaSemana) {
        return diaSemana == DiaSemana.SABADO
                ? CIERRE_SABADO
                : CIERRE_LUNES_A_VIERNES;
    }

    private void actualizarDatos(
            HorarioAtencion horario,
            Profesional profesional,
            HorarioAtencionRequest request
    ) {
        horario.setProfesional(profesional);
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraDesde(request.horaDesde());
        horario.setHoraHasta(request.horaHasta());
        horario.setDuracionTurno(request.duracionTurno());
    }

    private HorarioAtencionResponse convertirAResponse(
            HorarioAtencion horario
    ) {
        Profesional profesional = horario.getProfesional();

        return new HorarioAtencionResponse(
                horario.getId(),
                profesional.getId(),
                profesional.getNombre(),
                profesional.getApellido(),
                horario.getDiaSemana(),
                horario.getHoraDesde(),
                horario.getHoraHasta(),
                horario.getDuracionTurno(),
                horario.getActivo()
        );
    }
}