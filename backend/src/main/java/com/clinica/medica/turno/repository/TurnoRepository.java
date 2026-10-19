package com.clinica.medica.turno.repository;

import com.clinica.medica.turno.model.EstadoTurno;
import com.clinica.medica.turno.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findAllByOrderByFechaAscHoraInicioAsc();

    List<Turno> findByProfesionalIdAndFechaAndEstadoNotOrderByHoraInicioAsc(
            Long profesionalId,
            LocalDate fecha,
            EstadoTurno estado
    );

    @Query("""
            SELECT t
            FROM Turno t
            WHERE (:pacienteId IS NULL OR t.paciente.id = :pacienteId)
              AND (:profesionalId IS NULL OR t.profesional.id = :profesionalId)
              AND (:fecha IS NULL OR t.fecha = :fecha)
              AND (:estado IS NULL OR t.estado = :estado)
            ORDER BY t.fecha ASC, t.horaInicio ASC
            """)
    List<Turno> buscar(
            @Param("pacienteId") Long pacienteId,
            @Param("profesionalId") Long profesionalId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoTurno estado
    );

    @Query("""
            SELECT COUNT(t)
            FROM Turno t
            WHERE t.profesional.id = :profesionalId
              AND t.fecha = :fecha
              AND t.estado <> :estadoCancelado
              AND t.horaInicio < :horaFin
              AND t.horaFin > :horaInicio
              AND (:turnoId IS NULL OR t.id <> :turnoId)
            """)
    long contarSuperpuestos(
            @Param("profesionalId") Long profesionalId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("estadoCancelado") EstadoTurno estadoCancelado,
            @Param("turnoId") Long turnoId
    );
}