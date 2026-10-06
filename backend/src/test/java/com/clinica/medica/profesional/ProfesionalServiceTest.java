package com.clinica.medica.profesional;

import com.clinica.medica.especialidad.model.Especialidad;
import com.clinica.medica.especialidad.repository.EspecialidadRepository;
import com.clinica.medica.profesional.dto.ProfesionalRequest;
import com.clinica.medica.profesional.model.Profesional;
import com.clinica.medica.profesional.repository.ProfesionalRepository;
import com.clinica.medica.profesional.service.ProfesionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProfesionalServiceTest {

    private ProfesionalRepository repository;
    private EspecialidadRepository especialidadRepository;
    private ProfesionalService service;
    private Profesional profesional;

    @BeforeEach
    void preparar() {
        repository = mock(ProfesionalRepository.class);
        especialidadRepository = mock(EspecialidadRepository.class);
        
        service = new ProfesionalService(repository, especialidadRepository);

        profesional = new Profesional();
        profesional.setId(1L);
        profesional.setNombre("Ana");
        profesional.setApellido("Gómez");
        profesional.setMatricula("MP12345");
        profesional.setTelefono("3515551234");
        profesional.setEmail("ana.gomez@example.com");
        profesional.setActivo(true);
    }

    @Test
    void listarProfesionales() {
        when(repository.findAll()).thenReturn(List.of(profesional));

        var resultado = service.listar();

        assertEquals(1, resultado.size());
        assertEquals("MP12345", resultado.getFirst().matricula());
        verify(repository).findAll();
    }

    @Test
    void listarSinProfesionales() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.listar().isEmpty());
    }

    @Test
    void buscarProfesionalPorId() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        var resultado = service.buscarPorId(1L);

        assertEquals("Ana", resultado.nombre());
        assertEquals("Gómez", resultado.apellido());
        assertEquals("MP12345", resultado.matricula());
    }

    @Test
    void buscarProfesionalInexistenteDevuelve404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void buscarProfesionalPorMatricula() {
        when(repository.findByMatricula("MP12345"))
                .thenReturn(Optional.of(profesional));

        var resultado = service.buscarPorMatricula("MP12345");

        assertEquals("Ana", resultado.nombre());
        assertEquals("MP12345", resultado.matricula());
    }

    @Test
    void buscarPorMatriculaInexistenteDevuelve404() {
        when(repository.findByMatricula("NOEXISTE"))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.buscarPorMatricula("NOEXISTE")
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void crearProfesional() {
        when(repository.existsByMatricula("MP12345"))
                .thenReturn(false);

        when(repository.save(any(Profesional.class)))
                .thenAnswer(invocacion -> {
                    Profesional nuevo = invocacion.getArgument(0);
                    nuevo.setId(1L);
                    return nuevo;
                });

        var resultado = service.crear(request("MP12345"));

        assertEquals("Ana", resultado.nombre());
        assertEquals("Gómez", resultado.apellido());
        assertEquals("MP12345", resultado.matricula());
        assertTrue(resultado.activo());
        assertNotNull(resultado.fechaAlta());

        verify(repository).save(any(Profesional.class));
    }

    @Test
    void crearConMatriculaDuplicadaDevuelve409() {
        when(repository.existsByMatricula("MP12345"))
                .thenReturn(true);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.crear(request("MP12345"))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(repository, never()).save(any(Profesional.class));
    }

    @Test
    void actualizarProfesional() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(repository.existsByMatricula("MP99999"))
                .thenReturn(false);

        when(repository.save(any(Profesional.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var resultado = service.actualizar(1L, request("MP99999"));

        assertEquals("MP99999", resultado.matricula());
        assertEquals("3515551234", resultado.telefono());

        verify(repository).existsByMatricula("MP99999");
        verify(repository).save(profesional);
    }

    @Test
    void actualizarConMatriculaDuplicadaDevuelve409() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(repository.existsByMatricula("MP99999"))
                .thenReturn(true);

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(1L, request("MP99999"))
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("MP12345", profesional.getMatricula());

        verify(repository, never()).save(any(Profesional.class));
    }

    @Test
    void actualizarProfesionalInexistenteDevuelve404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.actualizar(99L, request("MP99999"))
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).save(any(Profesional.class));
    }

    @Test
    void eliminarRealizaBajaLogica() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        service.eliminar(1L);

        assertFalse(profesional.getActivo());

        verify(repository).save(profesional);
        verify(repository, never()).delete(any(Profesional.class));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void eliminarProfesionalInexistenteDevuelve404() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        var error = assertThrows(
                ResponseStatusException.class,
                () -> service.eliminar(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(repository, never()).save(any(Profesional.class));
    }

    @Test
    void crearRecortaEspaciosEnNombreApellidoYMatricula() {
        when(repository.save(any(Profesional.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        var datos = new ProfesionalRequest(
                " Ana ",
                " Gómez ",
                " MP12345 ",
                "3515551234",
                "ana.gomez@example.com"
        );

        var resultado = service.crear(datos);

        assertEquals("Ana", resultado.nombre());
        assertEquals("Gómez", resultado.apellido());
        assertEquals("MP12345", resultado.matricula());
    }

    private ProfesionalRequest request(String matricula) {
        return new ProfesionalRequest(
                "Ana",
                "Gómez",
                matricula,
                "3515551234",
                "ana.gomez@example.com"
        );
    }

    @Test
    void asociarEspecialidadAProfesional() {

        Especialidad especialidad = new Especialidad();
        especialidad.setId(1L);
        especialidad.setNombre("Cardiología");
        especialidad.setActivo(true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(especialidadRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(especialidad));

        when(repository.save(any(Profesional.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        service.asociarEspecialidad(1L, 1L);

        assertEquals(1, profesional.getEspecialidades().size());
        assertTrue(profesional.getEspecialidades().contains(especialidad));

        verify(repository).save(profesional);
    }

    @Test
    void asociarEspecialidadDuplicadaDevuelve409() {

        Especialidad especialidad = new Especialidad();
        especialidad.setId(1L);
        especialidad.setNombre("Cardiología");
        especialidad.setActivo(true);

        profesional.getEspecialidades().add(especialidad);

        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(especialidadRepository.findByIdAndActivoTrue(1L))
                .thenReturn(Optional.of(especialidad));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.asociarEspecialidad(1L, 1L)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

        verify(repository, never()).save(any(Profesional.class));
    }

    @Test
    void asociarEspecialidadInexistenteDevuelve404() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(profesional));

        when(especialidadRepository.findByIdAndActivoTrue(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.asociarEspecialidad(1L, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        verify(repository, never()).save(any(Profesional.class));
    }

    @Test
    void asociarAProfesionalInexistenteDevuelve404() {

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> service.asociarEspecialidad(99L, 1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        verify(especialidadRepository, never())
                .findByIdAndActivoTrue(anyLong());

        verify(repository, never())
                .save(any(Profesional.class));
    }

}