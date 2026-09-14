package com.clinica.medica.especialidad.model;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidades")
public class Especialidad {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 100, unique = true)
    private String nombre;

    @Column (length = 255)
    private String descripcion;

    @Column (nullable = false)
    private boolean activo = true;

    public Especialidad() {}

    public Long getId() { return id;}
    public void setId(Long id) { this.id = id;}

    public String getNombre() { return nombre;}
    public void setNombre(String nombre) { this.nombre = nombre;}

    public String getDescripcion() { return descripcion;}
    public void setDescripcion(String descripcion) { this.descripcion = descripcion;}

    public boolean isActivo() { return activo;}
    public void setActivo(boolean activo) { this.activo = activo;}
}