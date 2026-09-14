package com.clinica.medica.especialidad.repository;

import com.clinica.medica.especialidad.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    List<Especialidad> findAllByActivoTrue();
    Optional<Especialidad> findByIdAndActivoTrue(Long id);

    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
