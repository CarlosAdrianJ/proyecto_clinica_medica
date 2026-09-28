package com.clinica.medica.horario.repository;

import com.clinica.medica.horario.model.DiaSemana;
import com.clinica.medica.horario.model.HorarioAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface HorarioAtencionRepository
        extends JpaRepository<HorarioAtencion, Long> {

    List<HorarioAtencion> findByActivoTrue();

    List<HorarioAtencion> findByProfesionalIdAndActivoTrue(
            Long profesionalId
    );

    @Query("""
            SELECT COUNT(h)
            FROM HorarioAtencion h
            WHERE h.profesional.id = :profesionalId
              AND h.diaSemana = :diaSemana
              AND h.activo = true
              AND h.horaDesde < :horaHasta
              AND h.horaHasta > :horaDesde
              AND (:horarioId IS NULL OR h.id <> :horarioId)
            """)
    long contarSuperpuestos(
            @Param("profesionalId") Long profesionalId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaDesde") LocalTime horaDesde,
            @Param("horaHasta") LocalTime horaHasta,
            @Param("horarioId") Long horarioId
    );
}