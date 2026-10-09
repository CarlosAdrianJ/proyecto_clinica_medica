package com.clinica.medica.especialidad.service;

import com.clinica.medica.especialidad.model.Especialidad;
import com.clinica.medica.especialidad.repository.EspecialidadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadServiceTest {

    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private EspecialidadService especialidadService;

    @Test
    void crear_deberiaGuardarEspecialidad_cuandoNombreNoExiste() {
        Especialidad nueva = new Especialidad();
        nueva.setNombre("Cardiología");
        nueva.setDescripcion("Atención cardiovascular");

        when(especialidadRepository.existsByNombre("Cardiología")).thenReturn(false);
        when(especialidadRepository.save(nueva)).thenReturn(nueva);

        Especialidad resultado = especialidadService.crear(nueva);

        assertThat(resultado.getNombre()).isEqualTo("Cardiología");
        verify(especialidadRepository).save(nueva);
    }

    @Test
    void crear_deberiaLanzarExcepcion_cuandoNombreYaExiste() {
        Especialidad nueva = new Especialidad();
        nueva.setNombre("Cardiología");

        when(especialidadRepository.existsByNombre("Cardiología")).thenReturn(true);

        ResponseStatusException excepcion = assertThrows(
                ResponseStatusException.class,
                () -> especialidadService.crear(nueva)
        );

        verify(especialidadRepository, never()).save(any());
        assertThat(excepcion.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void listar_deberiaRetornarSoloActivas() {
        Especialidad e1 = new Especialidad();
        e1.setNombre("Cardiología");

        Especialidad e2 = new Especialidad();
        e2.setNombre("Dermatología");

        when(especialidadRepository.findAllByActivoTrue()).thenReturn(List.of(e1, e2));

        List<Especialidad> resultado = especialidadService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).containsExactly(e1, e2);
        verify(especialidadRepository).findAllByActivoTrue();
    }

    @Test
    void buscarPorId_deberiaRetornarEspecialidad_cuandoExiste() {
        Especialidad especialidad = new Especialidad();
        especialidad.setId(1L);
        especialidad.setNombre("Cardiología");

        when(especialidadRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(especialidad));

        Especialidad resultado = especialidadService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Cardiología");
    }

    @Test
    void buscarPorId_deberiaLanzarExcepcion_cuandoNoExiste() {
        final Long id = 999L;
        when(especialidadRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> especialidadService.buscarPorId(id));
    }

    @Test
    void actualizar_deberiaActualizarDatos_cuandoNombreNoEstaDuplicado() {
        Long id = 1L;

        Especialidad existente = new Especialidad();
        existente.setId(id);
        existente.setNombre("Cardiología");
        existente.setDescripcion("Descripción vieja");

        Especialidad datosNuevos = new Especialidad();
        datosNuevos.setNombre("Cardiología Infantil");
        datosNuevos.setDescripcion("Descripción nueva");

        when(especialidadRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(existente));
        when(especialidadRepository.existsByNombreAndIdNot("Cardiología Infantil", id)).thenReturn(false);
        when(especialidadRepository.save(existente)).thenReturn(existente);

        Especialidad resultado = especialidadService.actualizar(id, datosNuevos);

        assertThat(resultado.getNombre()).isEqualTo("Cardiología Infantil");
        assertThat(resultado.getDescripcion()).isEqualTo("Descripción nueva");
        verify(especialidadRepository).save(existente);
    }

    @Test
    void actualizar_deberiaLanzarExcepcion_cuandoNombreYaExisteEnOtraEspecialidad() {
        Long id = 1L;

        Especialidad existente = new Especialidad();
        existente.setId(id);
        existente.setNombre("Cardiología");

        Especialidad datosNuevos = new Especialidad();
        datosNuevos.setNombre("Dermatología");

        when(especialidadRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(existente));
        when(especialidadRepository.existsByNombreAndIdNot("Dermatología", id)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> especialidadService.actualizar(id, datosNuevos));

        verify(especialidadRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaMarcarComoInactiva() {
        Long id = 1L;
        Especialidad existente = new Especialidad();
        existente.setId(id);
        existente.setActivo(true);

        when(especialidadRepository.findByIdAndActivoTrue(id)).thenReturn(Optional.of(existente));

        especialidadService.eliminar(id);

        ArgumentCaptor<Especialidad> captor = ArgumentCaptor.forClass(Especialidad.class);
        verify(especialidadRepository).save(captor.capture());
        assertThat(captor.getValue().isActivo()).isFalse();
    }
}
