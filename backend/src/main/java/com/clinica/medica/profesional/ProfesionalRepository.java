package com.clinica.medica.profesional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {

    Optional<Profesional> findByMatricula(String matricula);
    
    boolean existsByMatricula(String matricula);

}
