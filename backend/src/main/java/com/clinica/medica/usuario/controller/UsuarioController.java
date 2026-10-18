package com.clinica.medica.usuario.controller;

import com.clinica.medica.usuario.dto.*;
import com.clinica.medica.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }
    @GetMapping public List<UsuarioResponse> listar() { return service.listar(); }
    @GetMapping("/{id}") public UsuarioResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
    }
    @PutMapping("/{id}") public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest r) {
        return service.actualizar(id, r);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
