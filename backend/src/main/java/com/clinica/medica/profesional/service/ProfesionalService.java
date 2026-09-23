package com.clinica.medica.profesional.service;

import com.clinica.medica.profesional.model.Profesional;

import org.springframework.stereotype.Service;

import com.clinica.medica.profesional.repository.ProfesionalRepository;

import java.util.List;
import java.util.Optional;


@Service 

public class ProfesionalService {

    private final ProfesionalRepository profesionalRepository;

    public ProfesionalService(ProfesionalRepository profesionalRepository) {
        this.profesionalRepository = profesionalRepository;
    }

    public Profesional crearProfesional(Profesional profesional) {
        if (profesionalRepository.existsByMatricula(profesional.getMatricula())) {
            throw new RuntimeException("Ya existe un profesional con esa matricula");
        }

        return profesionalRepository.save(profesional);
    
    }
    public List<Profesional> listarProfesionales() {
        return profesionalRepository.findAll();
    }

    public Profesional buscarPorId(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Profesional no encontrado"));
    }

    public Optional<Profesional> buscarPorMatricula(String matricula) {
        return profesionalRepository.findByMatricula(matricula);
    }

    public Profesional modificarProfesional(Long id, Profesional datosActualizados) {
        Profesional profesional = buscarPorId(id);

       if (!profesional.getMatricula().equals(datosActualizados.getMatricula())
        && profesionalRepository.existsByMatricula(datosActualizados.getMatricula())) {
            throw new RuntimeException("Ya existe un profesional con esa matricula");
        }

        profesional.setNombre(datosActualizados.getNombre());
        profesional.setApellido(datosActualizados.getApellido());
        profesional.setMatricula(datosActualizados.getMatricula());
        profesional.setTelefono(datosActualizados.getTelefono());
        profesional.setEmail(datosActualizados.getEmail());

        return profesionalRepository.save(profesional);
    }

    public Profesional darDeBaja(Long id) {
        Profesional profesional = buscarPorId(id);

        profesional.setActivo(false);

        return profesionalRepository.save(profesional);
    }


}