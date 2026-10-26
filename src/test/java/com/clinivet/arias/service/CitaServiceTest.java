package com.clinivet.arias.service;

import com.clinivet.arias.dto.CitaRequest;
import com.clinivet.arias.dto.CitaResponse;
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
}
