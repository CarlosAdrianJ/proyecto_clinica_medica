package com.clinica.medica.disponibilidad.controller;

import com.clinica.medica.disponibilidad.dto.DisponibilidadResponse;
import com.clinica.medica.disponibilidad.service.DisponibilidadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(
            DisponibilidadService disponibilidadService
    ) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping
    public DisponibilidadResponse consultar(
            @RequestParam Long profesionalId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return disponibilidadService.consultar(
                profesionalId,
                fecha
        );
    }
}