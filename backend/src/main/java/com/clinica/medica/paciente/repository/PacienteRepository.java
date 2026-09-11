package com.clinica.medica.paciente.repository;

import com.clinica.medica.paciente.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    List<Paciente> findAllByActivoTrue();

    Optional<Paciente> findByIdAndActivoTrue(Long id);

    boolean existsByDni(String dni);

    boolean existsByDniAndIdNot(String dni, Long id);
}