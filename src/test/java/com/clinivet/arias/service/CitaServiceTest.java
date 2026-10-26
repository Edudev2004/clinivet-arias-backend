package com.clinivet.arias.service;

import com.clinivet.arias.dto.CitaRequest;
import com.clinivet.arias.dto.CitaResponse;
import com.clinivet.arias.exception.HorarioOcupadoException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.*;
import com.clinivet.arias.repository.*;
import com.clinivet.arias.service.impl.CitaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CitaServiceImpl} utilizando JUnit 5 y Mockito.
 * Valida los criterios de aceptación de RF-13 (Registro de nueva cita) y reglas de negocio asociadas.
 */
@ExtendWith(MockitoExtension.class)
class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CitaServiceImpl citaService;

    private Propietario propietarioMock;
    private Mascota mascotaMock;
    private Veterinario veterinarioMock;
    private Usuario usuarioVetMock;
    private Usuario usuarioAdminMock;

    private final UUID propId = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final UUID mascId = UUID.fromString("f0000000-0000-0000-0000-000000000001");
    private final UUID vetId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private final UUID userVetId = UUID.fromString("b0000000-0000-0000-0000-000000000002");
    private final UUID adminId = UUID.fromString("b0000000-0000-0000-0000-000000000001");
    private final UUID citaId = UUID.fromString("70000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        Rol rolAdmin = new Rol(UUID.randomUUID(), "ADMINISTRADOR", "Acceso total");
        Rol rolVet = new Rol(UUID.randomUUID(), "VETERINARIO", "Atención médica");

        usuarioAdminMock = new Usuario(adminId, "Rommel", "Navarro", "admin@clinivet.pe", "hash", rolAdmin, true);
        usuarioVetMock = new Usuario(userVetId, "Beckhan", "Elias", "veterinario@clinivet.pe", "hash", rolVet, true);

        Especialidad espGral = new Especialidad(UUID.randomUUID(), "Medicina General", "Consultas generales");
        veterinarioMock = new Veterinario(vetId, usuarioVetMock, espGral, "CMVP-14892", true);

        propietarioMock = new Propietario(propId, "Carlos", "Mendoza Ramos", "45892314", "987123456", "carlos@gmail.com", "Av. Larco 456", true);
        mascotaMock = new Mascota(mascId, propietarioMock, "Bobby", "Canino", "Golden Retriever", "Dorado", "M", LocalDate.of(2022, 4, 10), new BigDecimal("28.50"), true);
    }

    @Test
    @DisplayName("RF-13: Registro de cita exitoso crea la cita con estado inicial PROGRAMADA y datos completos")
    void registrarCita_DatosValidos_CreaCitaConEstadoProgramadaYRetornaDetalle() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, hora, "Chequeo general preventivo");

        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.of(propietarioMock));
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioMock));
        when(usuarioRepository.findByEmail("admin@clinivet.pe")).thenReturn(Optional.of(usuarioAdminMock));

        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> {
            Cita guardada = invocation.getArgument(0);
            guardada.setId(citaId);
            return guardada;
        });

        // Act
        CitaResponse response = citaService.registrarCita(request, "admin@clinivet.pe");

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals(mascId, response.getMascotaId());
        assertEquals("Bobby", response.getNombreMascota());
        assertEquals("Canino", response.getEspecieMascota());
        assertEquals(propId, response.getPropietarioId());
        assertEquals("Carlos Mendoza Ramos", response.getNombrePropietario());
        assertEquals(vetId, response.getVeterinarioId());
        assertEquals("Beckhan Elias", response.getNombreVeterinario());
        assertEquals("Medicina General", response.getEspecialidadVeterinario());
        assertEquals(fecha, response.getFecha());
        assertEquals(hora, response.getHora());
        assertEquals("PROGRAMADA", response.getEstado());
        assertEquals("Chequeo general preventivo", response.getMotivo());
        assertEquals(adminId, response.getCreadoPorId());
        assertEquals("admin@clinivet.pe", response.getCreadoPorEmail());

        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("RF-13: Intento de agendar con mascota inexistente lanza ResourceNotFoundException (404)")
    void registrarCita_MascotaInexistente_LanzaResourceNotFoundException() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(11, 0), "Motivo");
        when(mascotaRepository.findById(mascId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("Mascota no encontrada"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Intento de agendar con propietario inexistente lanza ResourceNotFoundException (404)")
    void registrarCita_PropietarioInexistente_LanzaResourceNotFoundException() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(11, 0), "Motivo");
        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("Propietario no encontrado"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Intento de agendar con veterinario inexistente lanza ResourceNotFoundException (404)")
    void registrarCita_VeterinarioInexistente_LanzaResourceNotFoundException() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(11, 0), "Motivo");
        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.of(propietarioMock));
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("Veterinario no encontrado"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Mascota no pertenece al propietario indicado lanza IllegalArgumentException (400)")
    void registrarCita_MascotaNoPerteneceAPropietario_LanzaIllegalArgumentException() {
        // Arrange
        UUID otroPropId = UUID.randomUUID();
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, otroPropId, vetId, fecha, LocalTime.of(11, 0), "Motivo");

        Propietario otroPropietario = new Propietario(otroPropId, "Juan", "Perez", "12345678", "999888777", "juan@gmail.com", "Dir", true);

        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(otroPropId)).thenReturn(Optional.of(otroPropietario));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("no pertenece al propietario"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Mascota desactivada/inactiva lanza IllegalArgumentException (400)")
    void registrarCita_MascotaInactiva_LanzaIllegalArgumentException() {
        // Arrange
        mascotaMock.setActivo(false);
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(11, 0), "Motivo");

        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.of(propietarioMock));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("se encuentra inactiva"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Veterinario inactivo lanza IllegalArgumentException (400)")
    void registrarCita_VeterinarioInactivo_LanzaIllegalArgumentException() {
        // Arrange
        veterinarioMock.setActivo(false);
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(11, 0), "Motivo");

        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.of(propietarioMock));
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioMock));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("se encuentra inactivo"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Fecha u hora en el pasado lanza IllegalArgumentException (400)")
    void registrarCita_FechaEnElPasado_LanzaIllegalArgumentException() {
        // Arrange
        LocalDate fechaPasada = LocalDate.now().minusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fechaPasada, LocalTime.of(10, 0), "Motivo");

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertTrue(ex.getMessage().contains("fecha u hora pasada"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-13: Consultar cita por ID retorna detalle completo")
    void obtenerCitaPorId_CitaExiste_RetornaCitaResponse() {
        // Arrange
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        Cita cita = new Cita(citaId, mascId, veterinarioMock, fechaHora, EstadoCita.PROGRAMADA, "Control", adminId);
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(usuarioRepository.findById(adminId)).thenReturn(Optional.of(usuarioAdminMock));

        // Act
        CitaResponse response = citaService.obtenerCitaPorId(citaId);

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals("Bobby", response.getNombreMascota());
        assertEquals("PROGRAMADA", response.getEstado());
    }

    @Test
    @DisplayName("RF-13: Consultar cita inexistente lanza ResourceNotFoundException")
    void obtenerCitaPorId_CitaInexistente_LanzaResourceNotFoundException() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.obtenerCitaPorId(citaId));
    }

    @Test
    @DisplayName("RF-15 / RN-03: Intento de cita duplicada para el mismo veterinario en misma fecha y hora lanza HorarioOcupadoException (409)")
    void registrarCita_VeterinarioConCitaProgramada_LanzaHorarioOcupadoException() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, hora, "Chequeo");

        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.of(propietarioMock));
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioMock));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstado(vetId, fechaHora, EstadoCita.PROGRAMADA))
                .thenReturn(true);

        // Act & Assert
        HorarioOcupadoException ex = assertThrows(HorarioOcupadoException.class, () ->
                citaService.registrarCita(request, "admin@clinivet.pe"));
        assertEquals("El veterinario ya tiene una cita registrada en esa fecha y hora", ex.getMessage());
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-15 / RN-07: Si el veterinario tenía una cita CANCELADA a esa misma hora, permite agendar sin conflicto")
    void registrarCita_VeterinarioConCitaCancelada_PermiteAgendarCita() {
        // Arrange
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, hora, "Nueva cita tras cancelación");

        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(propietarioRepository.findById(propId)).thenReturn(Optional.of(propietarioMock));
        when(veterinarioRepository.findById(vetId)).thenReturn(Optional.of(veterinarioMock));
        when(usuarioRepository.findByEmail("admin@clinivet.pe")).thenReturn(Optional.of(usuarioAdminMock));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstado(vetId, fechaHora, EstadoCita.PROGRAMADA))
                .thenReturn(false);

        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> {
            Cita guardada = invocation.getArgument(0);
            guardada.setId(citaId);
            return guardada;
        });

        // Act
        CitaResponse response = citaService.registrarCita(request, "admin@clinivet.pe");

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals("PROGRAMADA", response.getEstado());
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("RF-16: Reprogramación exitosa actualiza fecha y hora cuando el estado es PROGRAMADA y hay disponibilidad")
    void reprogramarCita_DatosValidos_ActualizaFechaHoraYRetornaDetalle() {
        // Arrange
        LocalDateTime fechaHoraOriginal = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        Cita citaExistente = new Cita(citaId, mascId, veterinarioMock, fechaHoraOriginal, EstadoCita.PROGRAMADA, "Control", adminId);

        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        LocalTime nuevaHora = LocalTime.of(16, 0);
        LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaExistente));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstadoAndIdNot(vetId, nuevaFechaHora, EstadoCita.PROGRAMADA, citaId))
                .thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(usuarioRepository.findById(adminId)).thenReturn(Optional.of(usuarioAdminMock));

        // Act
        CitaResponse response = citaService.reprogramarCita(citaId, request);

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals(nuevaFecha, response.getFecha());
        assertEquals(nuevaHora, response.getHora());
        assertEquals("PROGRAMADA", response.getEstado());
        verify(citaRepository).save(citaExistente);
    }

    @Test
    @DisplayName("RF-16: Reprogramación con conflicto de horario del veterinario lanza HorarioOcupadoException (409)")
    void reprogramarCita_ConflictoHorarioVeterinario_LanzaHorarioOcupadoException() {
        // Arrange
        LocalDateTime fechaHoraOriginal = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        Cita citaExistente = new Cita(citaId, mascId, veterinarioMock, fechaHoraOriginal, EstadoCita.PROGRAMADA, "Control", adminId);

        LocalDate nuevaFecha = LocalDate.now().plusDays(3);
        LocalTime nuevaHora = LocalTime.of(11, 0);
        LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaExistente));
        when(citaRepository.existsByVeterinarioIdAndFechaHoraAndEstadoAndIdNot(vetId, nuevaFechaHora, EstadoCita.PROGRAMADA, citaId))
                .thenReturn(true);

        // Act & Assert
        HorarioOcupadoException ex = assertThrows(HorarioOcupadoException.class, () ->
                citaService.reprogramarCita(citaId, request));
        assertEquals("El veterinario ya tiene una cita registrada en esa fecha y hora", ex.getMessage());
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-16: Reprogramar una cita en estado CANCELADA lanza IllegalStateException (400)")
    void reprogramarCita_CitaCancelada_LanzaIllegalStateException() {
        // Arrange
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        Cita citaCancelada = new Cita(citaId, mascId, veterinarioMock, fechaHora, EstadoCita.CANCELADA, "Control", adminId);

        LocalDate nuevaFecha = LocalDate.now().plusDays(4);
        LocalTime nuevaHora = LocalTime.of(10, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaCancelada));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                citaService.reprogramarCita(citaId, request));
        assertTrue(ex.getMessage().contains("Solo se pueden reprogramar citas con estado PROGRAMADA"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-16: Reprogramar una cita en estado ATENDIDA lanza IllegalStateException (400)")
    void reprogramarCita_CitaAtendida_LanzaIllegalStateException() {
        // Arrange
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        Cita citaAtendida = new Cita(citaId, mascId, veterinarioMock, fechaHora, EstadoCita.ATENDIDA, "Control", adminId);

        LocalDate nuevaFecha = LocalDate.now().plusDays(4);
        LocalTime nuevaHora = LocalTime.of(10, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaAtendida));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                citaService.reprogramarCita(citaId, request));
        assertTrue(ex.getMessage().contains("Solo se pueden reprogramar citas con estado PROGRAMADA"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-16: Reprogramar a fecha u hora pasada lanza IllegalArgumentException (400)")
    void reprogramarCita_FechaPasada_LanzaIllegalArgumentException() {
        // Arrange
        LocalDateTime fechaHoraOriginal = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0);
        Cita citaExistente = new Cita(citaId, mascId, veterinarioMock, fechaHoraOriginal, EstadoCita.PROGRAMADA, "Control", adminId);

        LocalDate fechaPasada = LocalDate.now().minusDays(1);
        LocalTime hora = LocalTime.of(10, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(fechaPasada, hora);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaExistente));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                citaService.reprogramarCita(citaId, request));
        assertTrue(ex.getMessage().contains("fecha u hora pasada"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-16: Reprogramar cita inexistente lanza ResourceNotFoundException (404)")
    void reprogramarCita_CitaInexistente_LanzaResourceNotFoundException() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(
                LocalDate.now().plusDays(2), LocalTime.of(10, 0));

        assertThrows(ResourceNotFoundException.class, () -> citaService.reprogramarCita(citaId, request));
    }

    @Test
    @DisplayName("RF-17 / RN-07: Cancelación exitosa actualiza el estado a CANCELADA y conserva el registro en BD (soft cancel)")
    void cancelarCita_CitaProgramada_ActualizaEstadoACanceladaYConservaRegistro() {
        // Arrange
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        Cita citaProgramada = new Cita(citaId, mascId, veterinarioMock, fechaHora, EstadoCita.PROGRAMADA, "Chequeo", adminId);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaProgramada));
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mascotaRepository.findById(mascId)).thenReturn(Optional.of(mascotaMock));
        when(usuarioRepository.findById(adminId)).thenReturn(Optional.of(usuarioAdminMock));

        // Act
        CitaResponse response = citaService.cancelarCita(citaId);

        // Assert
        assertNotNull(response);
        assertEquals(citaId, response.getId());
        assertEquals("CANCELADA", response.getEstado());
        assertEquals(EstadoCita.CANCELADA, citaProgramada.getEstado());
        verify(citaRepository, times(1)).save(citaProgramada);
        verify(citaRepository, never()).delete(any());
        verify(citaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("RF-17: Cancelar una cita que ya se encuentra CANCELADA lanza IllegalStateException (400)")
    void cancelarCita_CitaYaCancelada_LanzaIllegalStateException() {
        // Arrange
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        Cita citaCancelada = new Cita(citaId, mascId, veterinarioMock, fechaHora, EstadoCita.CANCELADA, "Chequeo", adminId);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaCancelada));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> citaService.cancelarCita(citaId));
        assertTrue(ex.getMessage().contains("ya se encuentra cancelada"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-17: Cancelar una cita que ya fue ATENDIDA lanza IllegalStateException (400)")
    void cancelarCita_CitaAtendida_LanzaIllegalStateException() {
        // Arrange
        LocalDateTime fechaHora = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        Cita citaAtendida = new Cita(citaId, mascId, veterinarioMock, fechaHora, EstadoCita.ATENDIDA, "Chequeo", adminId);

        when(citaRepository.findById(citaId)).thenReturn(Optional.of(citaAtendida));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> citaService.cancelarCita(citaId));
        assertTrue(ex.getMessage().contains("ya fue atendida"));
        verify(citaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-17: Cancelar cita inexistente lanza ResourceNotFoundException (404)")
    void cancelarCita_CitaInexistente_LanzaResourceNotFoundException() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> citaService.cancelarCita(citaId));
    }
}
