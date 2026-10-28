package com.clinivet.arias.service;

import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;
import com.clinivet.arias.dto.DiagnosticoRequest;
import com.clinivet.arias.dto.DiagnosticoResponse;
import com.clinivet.arias.exception.ConsultaDuplicadaException;
import com.clinivet.arias.exception.DiagnosticoDuplicadoException;
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
    @DisplayName("RF-12: el peso acepta valores decimales precisos (ej. 3.75 kg) y actualiza la ficha de la mascota")
    void registrarConsulta_PesoDecimal_ActualizaFichaMascota() {
        BigDecimal pesoDecimal = new BigDecimal("3.75");
        request.setPeso(pesoDecimal);

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

        assertNotNull(response);
        assertEquals(pesoDecimal, response.getPesoKg());
        assertEquals(pesoDecimal, mascota.getPesoActualKg(), "El peso de la mascota debe actualizarse exactamente al valor decimal de la consulta");
        verify(mascotaRepository).save(mascota);
        verify(consultaRepository).save(any(Consulta.class));
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

    // ──────────────────────────────────────────────────────────────────────────
    // RF-21: PRUEBAS DE REGISTRO DE DIAGNÓSTICO POR CONSULTA
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RF-21: registro exitoso de diagnóstico vincula y persiste el diagnóstico en la consulta")
    void registrarDiagnostico_Exitoso() {
        Consulta consultaSinDiag = new Consulta();
        consultaSinDiag.setId(consultaId);
        consultaSinDiag.setCitaId(citaId);
        consultaSinDiag.setMotivo("Control");
        consultaSinDiag.setDiagnostico(""); // Sin diagnóstico inicial

        DiagnosticoRequest diagReq = new DiagnosticoRequest("Otitis externa bacteriana", "H60.9");
        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaSinDiag));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        DiagnosticoResponse response = consultaService.registrarDiagnostico(consultaId, diagReq);

        assertNotNull(response);
        assertEquals(consultaId, response.getConsultaId());
        assertEquals("Otitis externa bacteriana", response.getDescripcion());
        assertEquals("H60.9", response.getCodigoCIE());
        assertEquals("Otitis externa bacteriana", consultaSinDiag.getDiagnostico());
        verify(consultaRepository).save(consultaSinDiag);
    }

    @Test
    @DisplayName("RF-21: consulta no encontrada lanza ResourceNotFoundException (404)")
    void registrarDiagnostico_ConsultaNoExiste_Lanza404() {
        UUID noExisteId = UUID.randomUUID();
        DiagnosticoRequest diagReq = new DiagnosticoRequest("Diagnóstico válido", null);
        when(consultaRepository.findById(noExisteId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> consultaService.registrarDiagnostico(noExisteId, diagReq));
        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-21: consulta con diagnóstico previo lanza DiagnosticoDuplicadoException (409)")
    void registrarDiagnostico_Duplicado_Lanza409() {
        Consulta consultaConDiag = new Consulta();
        consultaConDiag.setId(consultaId);
        consultaConDiag.setDiagnostico("Diagnóstico previo ya asentado");

        DiagnosticoRequest diagReq = new DiagnosticoRequest("Nuevo intento de diagnóstico", null);
        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaConDiag));

        assertThrows(DiagnosticoDuplicadoException.class, () -> consultaService.registrarDiagnostico(consultaId, diagReq));
        verify(consultaRepository, never()).save(any());
    }
}
