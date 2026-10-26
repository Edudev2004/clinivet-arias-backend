package com.clinivet.arias.service;

import com.clinivet.arias.dto.DisponibilidadResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.*;
import com.clinivet.arias.repository.CitaRepository;
import com.clinivet.arias.repository.HorarioDisponibilidadRepository;
import com.clinivet.arias.repository.VeterinarioRepository;
import com.clinivet.arias.service.impl.VeterinarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link VeterinarioServiceImpl} utilizando Mockito.
 * Valida los criterios de aceptación de RF-14, RN-03 y RN-11:
 * - Consulta de disponibilidad disponible / no disponible
 * - Verificación contra horarios_disponibilidad (RN-11)
 * - Verificación de cruces con citas PROGRAMADA (RN-03 / RF-15)
 * - Citas CANCELADAS o ATENDIDAS no bloquean
 * - Veterinario inexistente (404) o inactivo
 */
@ExtendWith(MockitoExtension.class)
class VeterinarioServiceTest {

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private HorarioDisponibilidadRepository horarioDisponibilidadRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private VeterinarioServiceImpl veterinarioService;

    private final UUID vetId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private final UUID usuarioId = UUID.fromString("b0000000-0000-0000-0000-000000000001");
    private final UUID espId = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private final UUID horarioId = UUID.fromString("d0000000-0000-0000-0000-000000000001");

    private Veterinario veterinarioActivo;
    // 2026-11-02 es Lunes (diaSemana = 1)
    private final LocalDate fechaLunes = LocalDate.of(2026, 11, 2);
    private final LocalTime horaValida = LocalTime.of(10, 0, 0);

    @BeforeEach
    void setUp() {
        Rol rolVet = new Rol(UUID.fromString("e0000000-0000-0000-0000-000000000001"), "VETERINARIO", "Médico");
        Usuario usuario = new Usuario(usuarioId, "Beckhan", "Elias", "veterinario@clinivet.pe", "hash", rolVet, true);
        Especialidad esp = new Especialidad(espId, "Medicina General y Cirugía Menor", "Atención general");

        veterinarioActivo = new Veterinario(vetId, usuario, esp, "CMVP-14892", true);
    }

    @Test
    @DisplayName("RF-14: Veterinario en horario y sin citas programadas retorna disponible=true")
    void consultarDisponibilidad_EnHorarioYSinCitas_RetornaDisponibleTrue() {
        // Arrange
        HorarioDisponibilidad turno = new HorarioDisponibilidad(
                horarioId, veterinarioActivo, 1, LocalTime.of(9, 0), LocalTime.of(13, 0));

        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioActivo));
        when(horarioDisponibilidadRepository.findByVeterinarioIdAndDiaSemana(vetId, 1))
                .thenReturn(List.of(turno));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstado(
                eq(vetId), eq(LocalDateTime.of(fechaLunes, horaValida)), eq(EstadoCita.PROGRAMADA)))
                .thenReturn(false);

        // Act
        DisponibilidadResponse response = veterinarioService.consultarDisponibilidad(vetId, fechaLunes, horaValida);

        // Assert
        assertNotNull(response);
        assertTrue(response.isDisponible());
        assertEquals(vetId, response.getVeterinarioId());
        assertEquals("Dr. Beckhan Elias", response.getVeterinarioNombre());
        assertEquals("CMVP-14892", response.getNumeroColegiatura());
        assertEquals("Medicina General y Cirugía Menor", response.getEspecialidad());
        assertEquals(fechaLunes, response.getFecha());
        assertEquals(horaValida, response.getHora());
        assertFalse(response.getTurnosDelDia().isEmpty());
    }

    @Test
    @DisplayName("RF-14 / RN-03: Veterinario con cita PROGRAMADA a esa hora retorna disponible=false")
    void consultarDisponibilidad_ConCitaProgramada_RetornaDisponibleFalse() {
        // Arrange
        HorarioDisponibilidad turno = new HorarioDisponibilidad(
                horarioId, veterinarioActivo, 1, LocalTime.of(9, 0), LocalTime.of(13, 0));

        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioActivo));
        when(horarioDisponibilidadRepository.findByVeterinarioIdAndDiaSemana(vetId, 1))
                .thenReturn(List.of(turno));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstado(
                eq(vetId), eq(LocalDateTime.of(fechaLunes, horaValida)), eq(EstadoCita.PROGRAMADA)))
                .thenReturn(true);

        // Act
        DisponibilidadResponse response = veterinarioService.consultarDisponibilidad(vetId, fechaLunes, horaValida);

        // Assert
        assertNotNull(response);
        assertFalse(response.isDisponible());
        assertTrue(response.getMensaje().contains("ya cuenta con una cita médica programada"));
    }

    @Test
    @DisplayName("RF-14 / RN-11: Hora solicitada fuera de las franjas de turno retorna disponible=false")
    void consultarDisponibilidad_FueraDeTurno_RetornaDisponibleFalse() {
        // Turno solo de 09:00 a 13:00; hora consultada: 15:00
        LocalTime horaTarde = LocalTime.of(15, 0, 0);
        HorarioDisponibilidad turnoManana = new HorarioDisponibilidad(
                horarioId, veterinarioActivo, 1, LocalTime.of(9, 0), LocalTime.of(13, 0));

        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioActivo));
        when(horarioDisponibilidadRepository.findByVeterinarioIdAndDiaSemana(vetId, 1))
                .thenReturn(List.of(turnoManana));

        // Act
        DisponibilidadResponse response = veterinarioService.consultarDisponibilidad(vetId, fechaLunes, horaTarde);

        // Assert
        assertNotNull(response);
        assertFalse(response.isDisponible());
        assertTrue(response.getMensaje().contains("fuera del horario de atención"));
        verify(citaRepository, never()).existsByVeterinarioIdAndFechaHoraAndEstado(any(), any(), any());
    }

    @Test
    @DisplayName("RF-14 / RN-11: Día sin turnos configurados para el veterinario retorna disponible=false")
    void consultarDisponibilidad_DiaSinTurnos_RetornaDisponibleFalse() {
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioActivo));
        when(horarioDisponibilidadRepository.findByVeterinarioIdAndDiaSemana(vetId, 1))
                .thenReturn(Collections.emptyList());

        // Act
        DisponibilidadResponse response = veterinarioService.consultarDisponibilidad(vetId, fechaLunes, horaValida);

        // Assert
        assertNotNull(response);
        assertFalse(response.isDisponible());
        assertTrue(response.getMensaje().contains("no atiende los días Lunes"));
    }

    @Test
    @DisplayName("RF-14: Veterinario inexistente lanza ResourceNotFoundException (HTTP 404)")
    void consultarDisponibilidad_VeterinarioInexistente_LanzaResourceNotFound() {
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> veterinarioService.consultarDisponibilidad(vetId, fechaLunes, horaValida)
        );
    }

    @Test
    @DisplayName("RF-14: Veterinario inactivo lanza IllegalArgumentException")
    void consultarDisponibilidad_VeterinarioInactivo_LanzaIllegalArgumentException() {
        veterinarioActivo.setActivo(false);
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioActivo));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> veterinarioService.consultarDisponibilidad(vetId, fechaLunes, horaValida)
        );
        assertTrue(ex.getMessage().contains("se encuentra inactivo"));
    }

    @Test
    @DisplayName("RF-14: Método reutilizable esHorarioDisponible retorna booleano consistente")
    void esHorarioDisponible_EvaluaCorrectamente() {
        HorarioDisponibilidad turno = new HorarioDisponibilidad(
                horarioId, veterinarioActivo, 1, LocalTime.of(9, 0), LocalTime.of(13, 0));

        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioActivo));
        when(horarioDisponibilidadRepository.findByVeterinarioIdAndDiaSemana(vetId, 1))
                .thenReturn(List.of(turno));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstado(
                eq(vetId), eq(LocalDateTime.of(fechaLunes, horaValida)), eq(EstadoCita.PROGRAMADA)))
                .thenReturn(false);

        boolean disponible = veterinarioService.esHorarioDisponible(vetId, LocalDateTime.of(fechaLunes, horaValida));
        assertTrue(disponible);
    }
}
