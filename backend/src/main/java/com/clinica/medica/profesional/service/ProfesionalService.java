package com.clinica.medica.profesional.service;

import com.clinica.medica.profesional.dto.ProfesionalRequest;
import com.clinica.medica.profesional.dto.ProfesionalResponse;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfesionalService {

    private final ProfesionalRepository profesionalRepository;

    public ProfesionalService(ProfesionalRepository profesionalRepository) {
        this.profesionalRepository = profesionalRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfesionalResponse> listar() {
        return profesionalRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfesionalResponse buscarPorId(Long id) {
        Profesional profesional = obtenerProfesional(id);
        return convertirAResponse(profesional);
    }

    @Transactional(readOnly = true)
    public ProfesionalResponse buscarPorMatricula(String matricula) {
        Profesional profesional = profesionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profesional no encontrado"
                ));

        return convertirAResponse(profesional);
    }

    @Transactional
    public ProfesionalResponse crear(ProfesionalRequest request) {
        if (profesionalRepository.existsByMatricula(request.matricula())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un profesional con esa matrícula"
            );
        }

        Profesional profesional = new Profesional();
        actualizarDatos(profesional, request);

        return convertirAResponse(profesionalRepository.save(profesional));
    }

    @Transactional
    public ProfesionalResponse actualizar(Long id, ProfesionalRequest request) {
        Profesional profesional = obtenerProfesional(id);

        if (!profesional.getMatricula().equals(request.matricula())
                && profesionalRepository.existsByMatricula(request.matricula())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro profesional con esa matrícula"
            );
        }

        actualizarDatos(profesional, request);

        return convertirAResponse(profesionalRepository.save(profesional));
    }

    @Transactional
    public void eliminar(Long id) {
        Profesional profesional = obtenerProfesional(id);
        profesional.setActivo(false);
        profesionalRepository.save(profesional);
    }

    private Profesional obtenerProfesional(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profesional no encontrado"
                ));
    }

    private void actualizarDatos(Profesional profesional, ProfesionalRequest request) {
        profesional.setNombre(request.nombre().trim());
        profesional.setApellido(request.apellido().trim());
        profesional.setMatricula(request.matricula().trim());
        profesional.setTelefono(request.telefono());
        profesional.setEmail(request.email());
    }

    private ProfesionalResponse convertirAResponse(Profesional profesional) {
        return new ProfesionalResponse(
                profesional.getId(),
                profesional.getNombre(),
                profesional.getApellido(),
                profesional.getMatricula(),
                profesional.getTelefono(),
                profesional.getEmail(),
                profesional.getActivo(),
                profesional.getFechaAlta()
        );
    }
}