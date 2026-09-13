package com.clinica.medica.profesional;

import jakarta.validation.Valid;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController 
@RequestMapping("/api/profesionales")

public class ProfesionalController {

    private final ProfesionalService profesionalService;
    
    public ProfesionalController(ProfesionalService profesionalService) {
        this.profesionalService = profesionalService;
    }

    @PostMapping 
    public ResponseEntity<Profesional> crearProfesional(
        @Valid @RequestBody Profesional profesional) {

            Profesional nuevoProfesional =
                profesionalService.crearProfesional(profesional);

            return ResponseEntity.ok(nuevoProfesional);
        }

    @GetMapping 
    public ResponseEntity<List<Profesional>> listarProfesionales() {
        return ResponseEntity.ok(
            profesionalService.listarProfesionales()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profesional> buscarPorId(@PathVariable Long id) {

            return ResponseEntity.ok(
                profesionalService.buscarPorId(id)
            );
        }

    
    @PutMapping ("/{id}")
    public ResponseEntity<Profesional> modificarProfesional(
        @PathVariable Long id,
        @Valid @RequestBody Profesional profesional) {

            return ResponseEntity.ok(
                profesionalService.modificarProfesional(id, profesional)
            );
        }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<Optional<Profesional>> buscarPorMatricula(
        @PathVariable String matricula) {

            return ResponseEntity.ok(
                profesionalService.buscarPorMatricula(matricula)
            );
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<Profesional> darDeBaja(@PathVariable Long id) {
        return ResponseEntity.ok(
            profesionalService.darDeBaja(id)
        );
    }
    
}
