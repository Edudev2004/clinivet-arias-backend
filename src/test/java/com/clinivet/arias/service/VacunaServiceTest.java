package com.clinivet.arias.service;

import com.clinivet.arias.dto.VacunaAplicadaRequest;
import com.clinivet.arias.dto.VacunaAplicadaResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.*;
import com.clinivet.arias.repository.*;
import com.clinivet.arias.service.impl.VacunaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link VacunaServiceImpl} (RF-24) con Mockito (AAA).
 */
@ExtendWith(MockitoExtension.class)
class VacunaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private VacunaCatalogoRepository vacunaCatalogoRepository;

    @Mock
    private VacunaAplicadaRepository vacunaAplicadaRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private VacunaServiceImpl vacunaService;

    private UUID mascotaId;
    private UUID veterinarioId;
    private UUID vacunaId;
    private Mascota mascota;
    private Veterinario veterinario;
    private VacunaCatalogo vacunaCatalogo;

    @BeforeEach
    void setUp() {
        mascotaId = UUID.fromString("b0000000-0000-0000-0000-000000000001");
        veterinarioId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
        vacunaId = UUID.fromString("9a000000-0000-0000-0000-000000000001");

        mascota = new Mascota();
        mascota.setId(mascotaId);
        mascota.setNombre("Firulais");

        Usuario usuarioVet = new Usuario();
        usuarioVet.setNombre("Patricia");
        usuarioVet.setApellido("Gómez");

        veterinario = new Veterinario();
        veterinario.setId(veterinarioId);
        veterinario.setUsuario(usuarioVet);
        veterinario.setActivo(true);

        vacunaCatalogo = new VacunaCatalogo(vacunaId, "Séxtuple Canina", "Protección contra parvo, moquillo y adenovirus");
    }

    @Test
    @DisplayName("registrarVacuna registra exitosamente cuando la mascota y veterinario existen")
    void registrarVacuna_Exitoso() {
        // Arrange
        VacunaAplicadaRequest request = new VacunaAplicadaRequest(
                "Séxtuple Canina",
                LocalDate.of(2026, 10, 28),
                LocalDate.of(2027, 10, 28),
                veterinarioId,
                "LOT-2026-X1"
        );

        when(mascotaRepository.findById(mascotaId)).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.of(veterinario));
        when(vacunaCatalogoRepository.findByNombreIgnoreCase("Séxtuple Canina")).thenReturn(Optional.of(vacunaCatalogo));

        VacunaAplicada guardada = new VacunaAplicada(
                UUID.randomUUID(),
                mascota,
                vacunaCatalogo,
                null,
                veterinario,
                request.getFechaAplicacion(),
                request.getProximaFecha()
        );
        guardada.setLote("LOT-2026-X1");
        guardada.setCreatedAt(LocalDateTime.now());

        when(vacunaAplicadaRepository.save(any(VacunaAplicada.class))).thenReturn(guardada);

        // Act
        VacunaAplicadaResponse response = vacunaService.registrarVacuna(mascotaId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMascotaId()).isEqualTo(mascotaId);
        assertThat(response.getMascotaNombre()).isEqualTo("Firulais");
        assertThat(response.getNombreVacuna()).isEqualTo("Séxtuple Canina");
        assertThat(response.getVeterinarioNombre()).isEqualTo("Patricia Gómez");
        assertThat(response.getLote()).isEqualTo("LOT-2026-X1");
        verify(vacunaAplicadaRepository).save(any(VacunaAplicada.class));
    }

    @Test
    @DisplayName("registrarVacuna lanza ResourceNotFoundException cuando la mascota no existe")
    void registrarVacuna_MascotaNoExiste_LanzaException() {
        // Arrange
        VacunaAplicadaRequest request = new VacunaAplicadaRequest(
                "Antirrábica",
                LocalDate.now(),
                null,
                veterinarioId,
                null
        );
        when(mascotaRepository.findById(mascotaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vacunaService.registrarVacuna(mascotaId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mascota no encontrada");

        verify(vacunaAplicadaRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarVacuna lanza ResourceNotFoundException cuando el veterinario no existe")
    void registrarVacuna_VeterinarioNoExiste_LanzaException() {
        // Arrange
        VacunaAplicadaRequest request = new VacunaAplicadaRequest(
                "Antirrábica",
                LocalDate.now(),
                null,
                veterinarioId,
                null
        );
        when(mascotaRepository.findById(mascotaId)).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> vacunaService.registrarVacuna(mascotaId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Veterinario no encontrado");

        verify(vacunaAplicadaRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarVacuna lanza IllegalArgumentException cuando el veterinario está inactivo")
    void registrarVacuna_VeterinarioInactivo_LanzaException() {
        // Arrange
        veterinario.setActivo(false);
        VacunaAplicadaRequest request = new VacunaAplicadaRequest(
                "Antirrábica",
                LocalDate.now(),
                null,
                veterinarioId,
                null
        );
        when(mascotaRepository.findById(mascotaId)).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.of(veterinario));

        // Act & Assert
        assertThatThrownBy(() -> vacunaService.registrarVacuna(mascotaId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no se encuentra activo");

        verify(vacunaAplicadaRepository, never()).save(any());
    }

    @Test
    @DisplayName("listarPorMascota retorna lista de vacunas registradas ordenadas")
    void listarPorMascota_Exitoso() {
        // Arrange
        VacunaAplicada vacunaApp = new VacunaAplicada(
                UUID.randomUUID(),
                mascota,
                vacunaCatalogo,
                null,
                veterinario,
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 9, 1)
        );
        vacunaApp.setCreatedAt(LocalDateTime.now());

        when(mascotaRepository.existsById(mascotaId)).thenReturn(true);
        when(vacunaAplicadaRepository.findByMascotaIdOrderByFechaAplicacionDesc(mascotaId))
                .thenReturn(List.of(vacunaApp));

        // Act
        List<VacunaAplicadaResponse> resultado = vacunaService.listarPorMascota(mascotaId);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombreVacuna()).isEqualTo("Séxtuple Canina");
    }

    @Test
    @DisplayName("listarPorMascota lanza ResourceNotFoundException si la mascota no existe")
    void listarPorMascota_MascotaNoExiste_LanzaException() {
        when(mascotaRepository.existsById(mascotaId)).thenReturn(false);

        assertThatThrownBy(() -> vacunaService.listarPorMascota(mascotaId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Mascota no encontrada");
    }
}
