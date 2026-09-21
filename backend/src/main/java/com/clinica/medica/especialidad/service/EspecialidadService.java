package com.clinica.medica.especialidad.service;

import com.clinica.medica.especialidad.model.Especialidad;
import com.clinica.medica.especialidad.repository.EspecialidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class EspecialidadService {
    
    private final EspecialidadRepository especialidadRepository;

    public EspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> listar() {
        return especialidadRepository.findAllByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Especialidad buscarPorId(Long id) {
        return especialidadRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la especialidad con id: " + id
                ));
    }

    @Transactional
    public Especialidad crear(Especialidad especialidad) {
        if (especialidadRepository.existsByNombre(especialidad.getNombre())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una especialidad con ese nombre"
            );
        }
        return especialidadRepository.save(especialidad);
    }

    @Transactional 
    public void eliminar(Long id) {
        Especialidad especialidad = buscarPorId(id);
        especialidad.setActivo(false);
        especialidadRepository.save(especialidad);
    }

    @Transactional
    public Especialidad actualizar(Long id, Especialidad especialidad) {
        Especialidad existente = buscarPorId(id);

        if (especialidadRepository.existsByNombreAndIdNot(especialidad.getNombre(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra especialidad con ese nombre"
            );
        }

        existente.setNombre(especialidad.getNombre());
        existente.setDescripcion(especialidad.getDescripcion());

        return especialidadRepository.save(existente);
    }
}
