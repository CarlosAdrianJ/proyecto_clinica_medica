package com.clinica.medica.paciente.service;

import com.clinica.medica.paciente.dto.PacienteRequest;
import com.clinica.medica.paciente.dto.PacienteResponse;
import com.clinica.medica.paciente.model.Paciente;
import com.clinica.medica.paciente.repository.PacienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar() {
        return pacienteRepository.findAllByActivoTrue()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorId(Long id) {
        Paciente paciente = obtenerPacienteActivo(id);
        return convertirAResponse(paciente);
    }

    @Transactional
    public PacienteResponse crear(PacienteRequest request) {
        if (pacienteRepository.existsByDni(request.dni())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un paciente con ese DNI"
            );
        }

        Paciente paciente = new Paciente();
        actualizarDatos(paciente, request);

        return convertirAResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public PacienteResponse actualizar(Long id, PacienteRequest request) {
        Paciente paciente = obtenerPacienteActivo(id);

        if (pacienteRepository.existsByDniAndIdNot(request.dni(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro paciente con ese DNI"
            );
        }

        actualizarDatos(paciente, request);

        return convertirAResponse(pacienteRepository.save(paciente));
    }

    @Transactional
    public void eliminar(Long id) {
        Paciente paciente = obtenerPacienteActivo(id);
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
    }

    private Paciente obtenerPacienteActivo(Long id) {
        return pacienteRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paciente no encontrado"
                ));
    }

    private void actualizarDatos(Paciente paciente, PacienteRequest request) {
        paciente.setNombre(request.nombre().trim());
        paciente.setApellido(request.apellido().trim());
        paciente.setDni(request.dni().trim());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setTelefono(request.telefono());
        paciente.setEmail(request.email());
        paciente.setDireccion(request.direccion());
    }

    private PacienteResponse convertirAResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getDni(),
                paciente.getFechaNacimiento(),
                paciente.getTelefono(),
                paciente.getEmail(),
                paciente.getDireccion(),
                paciente.isActivo(),
                paciente.getFechaAlta()
        );
    }
}