package com.clinica.medica.security;

import com.clinica.medica.usuario.controller.UsuarioController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Evita devolver o registrar valores rechazados de campos de contrasena. */
@RestControllerAdvice(assignableTypes = {AuthController.class, UsuarioController.class})
public class ValidacionSeguridadHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Datos de usuario invalidos. Revise los campos obligatorios y sus limites.");
    }
}
