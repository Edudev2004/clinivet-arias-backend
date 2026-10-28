package com.clinivet.arias.service;

import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;
import com.clinivet.arias.exception.ConsultaDuplicadaException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Cita;
import com.clinivet.arias.model.Consulta;
import com.clinivet.arias.model.EstadoCita;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Veterinario;
import com.clinivet.arias.repository.CitaRepository;
import com.clinivet.arias.repository.ConsultaRepository;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.VeterinarioRepository;
import com.clinivet.arias.service.impl.ConsultaServiceImpl;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de {@link ConsultaServiceImpl} (RF-12): registro exitoso, cita inválida y campos faltantes.
 */
@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private CitaRepository citaRepository;
    @Mock
    private MascotaRepository mascotaRepository;
    @Mock
    private VeterinarioRepository veterinarioRepository;

    @InjectMocks
    private ConsultaServiceImpl consultaService;

    private final UUID citaId = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private final UUID veterinarioId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private final UUID mascotaId = UUID.fromString("f0000000-0000-0000-0000-000000000001");
    private final UUID consultaId = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final LocalDateTime fechaHoraCita = LocalDateTime.of(2026, 11, 15, 10, 30);

    private Veterinario veterinario;
    private Cita cita;
    private Mascota mascota;
    private ConsultaRequest request;

    @BeforeEach
    void setUp() {
        veterinario = new Veterinario(veterinarioId, null, null, "CMVP-1234", true);
        cita = new Cita(citaId, mascotaId, veterinario, fechaHoraCita, EstadoCita.PROGRAMADA, "Control", UUID.randomUUID());
        mascota = new Mascota();
        mascota.setId(mascotaId);
        mascota.setNombre("Firulais");
        mascota.setPesoActualKg(new BigDecimal("25.00"));

        request = new ConsultaRequest(
                citaId, veterinarioId, LocalDate.of(2026, 11, 15),
                "Control post-operatorio", "Herida en cicatrización adecuada",
                new BigDecimal("28.50"), new BigDecimal("38.5"), "Sin complicaciones");
    }

    @Test
    @DisplayName("RF-12: registro exitoso persiste la consulta, actualiza peso de la mascota y marca la cita ATENDIDA")
    void registrarConsulta_Exitoso() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.of(veterinario));
        when(consultaRepository.existsByCitaId(citaId)).thenReturn(false);
        when(mascotaRepository.findById(mascotaId)).thenReturn(Optional.of(mascota));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> {
            Consulta c = inv.getArgument(0);
            c.setId(consultaId);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        ConsultaResponse response = consultaService.registrarConsulta(request);

        assertEquals(consultaId, response.getId());
        assertEquals(citaId, response.getCitaId());
        assertEquals(mascotaId, response.getMascotaId());
        assertEquals(veterinarioId, response.getVeterinarioId());
        assertEquals(new BigDecimal("28.50"), response.getPesoKg());
        assertEquals(new BigDecimal("38.5"), response.getTemperaturaC());
        assertEquals("Sin complicaciones", response.getObservaciones());

        // RF-12: peso actual de la mascota actualizado
        assertEquals(new BigDecimal("28.50"), mascota.getPesoActualKg());
        verify(mascotaRepository).save(mascota);
        // La cita queda ATENDIDA
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
        verify(citaRepository).save(cita);
    }

    @Test
    @DisplayName("RF-12: cita inexistente lanza ResourceNotFoundException y no persiste nada")
    void registrarConsulta_CitaInexistente() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> consultaService.registrarConsulta(request));

        verify(consultaRepository, never()).save(any());
        verify(mascotaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-12: cita cancelada lanza IllegalArgumentException")
    void registrarConsulta_CitaCancelada() {
        cita.setEstado(EstadoCita.CANCELADA);
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.of(veterinario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> consultaService.registrarConsulta(request));

        assertTrue(ex.getMessage().contains("cancelada"));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-09: cita con consulta previa lanza ConsultaDuplicadaException")
    void registrarConsulta_CitaYaConConsulta() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.of(veterinario));
        when(consultaRepository.existsByCitaId(citaId)).thenReturn(true);

        assertThrows(ConsultaDuplicadaException.class, () -> consultaService.registrarConsulta(request));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RN-02: veterinario distinto al asignado en la cita lanza IllegalArgumentException")
    void registrarConsulta_VeterinarioNoAsignado() {
        UUID otroVetId = UUID.fromString("d0000000-0000-0000-0000-000000000099");
        Veterinario otro = new Veterinario(otroVetId, null, null, "CMVP-9999", true);
        request.setVeterinarioId(otroVetId);
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(veterinarioRepository.findById(otroVetId)).thenReturn(Optional.of(otro));

        assertThrows(IllegalArgumentException.class, () -> consultaService.registrarConsulta(request));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-12: veterinario inexistente lanza ResourceNotFoundException")
    void registrarConsulta_VeterinarioInexistente() {
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> consultaService.registrarConsulta(request));
    }

    @Test
    @DisplayName("RF-12: fecha distinta a la de la cita lanza IllegalArgumentException")
    void registrarConsulta_FechaNoCoincide() {
        request.setFecha(LocalDate.of(2026, 11, 20));
        when(citaRepository.findById(citaId)).thenReturn(Optional.of(cita));
        when(veterinarioRepository.findById(veterinarioId)).thenReturn(Optional.of(veterinario));
        when(consultaRepository.existsByCitaId(citaId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> consultaService.registrarConsulta(request));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-12: campos obligatorios faltantes (citaId y peso) son rechazados antes de tocar la BD")
    void registrarConsulta_CamposFaltantes() {
        request.setCitaId(null);
        assertThrows(NullPointerException.class, () -> consultaService.registrarConsulta(request));

        request.setCitaId(citaId);
        request.setPeso(null);
        assertThrows(NullPointerException.class, () -> consultaService.registrarConsulta(request));

        verifyNoInteractions(citaRepository, consultaRepository, mascotaRepository);
    }
}
