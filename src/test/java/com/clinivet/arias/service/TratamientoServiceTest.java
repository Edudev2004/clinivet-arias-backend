package com.clinivet.arias.service;

import com.clinivet.arias.dto.TratamientoCreateRequest;
import com.clinivet.arias.dto.TratamientoResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Consulta;
import com.clinivet.arias.model.Medicamento;
import com.clinivet.arias.model.Tratamiento;
import com.clinivet.arias.repository.ConsultaRepository;
import com.clinivet.arias.repository.MedicamentoRepository;
import com.clinivet.arias.repository.TratamientoRepository;
import com.clinivet.arias.service.impl.TratamientoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link TratamientoServiceImpl} con Mockito.
 * Valida los criterios de aceptación de RF-23:
 * - El cálculo usa el peso registrado en la consulta actual
 * - El resultado se almacena junto al tratamiento
 * - Si el peso no está registrado en la consulta, retorna HTTP 400
 */
@ExtendWith(MockitoExtension.class)
class TratamientoServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private TratamientoRepository tratamientoRepository;

    @Mock
    private IDosisCalculatorService dosisCalculatorService;

    @InjectMocks
    private TratamientoServiceImpl tratamientoService;

    private final UUID consultaId = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final UUID citaId = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private final UUID medicamentoId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private final UUID tratamientoId = UUID.fromString("f0000000-0000-0000-0000-000000000001");

    private Consulta consultaConPeso;
    private Consulta consultaSinPeso;
    private Medicamento medicamentoValido;

    @BeforeEach
    void setUp() {
        consultaConPeso = new Consulta(
                consultaId,
                citaId,
                new BigDecimal("28.50"), // Peso 28.50 kg
                new BigDecimal("38.5"),
                "Control post-operatorio",
                "Herida en cicatrización adecuada",
                "Sin complicaciones"
        );

        consultaSinPeso = new Consulta(
                consultaId,
                citaId,
                null, // Sin peso registrado
                new BigDecimal("38.5"),
                "Consulta rápida",
                "Revisión visual",
                null
        );

        medicamentoValido = new Medicamento(
                medicamentoId,
                "Amoxicilina + Clavulánico 250mg",
                "Amoxicilina / Ác. Clavulánico",
                "Suspensión 60ml",
                "FRASCO",
                new BigDecimal("5.00"),
                true
        );
    }

    @Test
    @DisplayName("RF-23: Prescripción calcula dosis automáticamente usando el peso de la consulta y persiste")
    void prescribirTratamiento_ConPesoEnConsulta_CalculaDosisYPersiste() {
        // Arrange
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("12.50"),
                7,
                "Administrar cada 12 horas vía oral"
        );

        BigDecimal dosisTotalCalculada = new BigDecimal("356.25");

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaConPeso));
        when(medicamentoRepository.findById(medicamentoId)).thenReturn(Optional.of(medicamentoValido));
        when(dosisCalculatorService.calcularDosisTotal(new BigDecimal("28.50"), new BigDecimal("12.50")))
                .thenReturn(dosisTotalCalculada);

        when(tratamientoRepository.save(any(Tratamiento.class))).thenAnswer(invocation -> {
            Tratamiento t = invocation.getArgument(0);
            t.setId(tratamientoId);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        // Act
        TratamientoResponse response = tratamientoService.prescribirTratamiento(consultaId, request);

        // Assert
        assertNotNull(response);
        assertEquals(tratamientoId, response.getId());
        assertEquals(consultaId, response.getConsultaId());
        assertEquals(medicamentoId, response.getMedicamentoId());
        assertEquals("Amoxicilina + Clavulánico 250mg", response.getMedicamentoNombre());
        assertEquals(new BigDecimal("28.50"), response.getPesoKgAplicado());
        assertEquals(new BigDecimal("12.50"), response.getDosisMgPorKg());
        assertEquals(dosisTotalCalculada, response.getDosisTotalMg());
        assertEquals(7, response.getDuracionDias());
        assertEquals("Administrar cada 12 horas vía oral", response.getIndicaciones());

        verify(dosisCalculatorService, times(1))
                .calcularDosisTotal(new BigDecimal("28.50"), new BigDecimal("12.50"));
        verify(tratamientoRepository, times(1)).save(any(Tratamiento.class));
    }

    @Test
    @DisplayName("RF-23: Prescripción en consulta sin peso registrado lanza IllegalArgumentException (HTTP 400)")
    void prescribirTratamiento_ConsultaSinPeso_LanzaIllegalArgumentException() {
        // Arrange
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("10.00"),
                5,
                "Cada 8 horas"
        );

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaSinPeso));

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> tratamientoService.prescribirTratamiento(consultaId, request)
        );

        assertTrue(ex.getMessage().contains("no tiene registrado el peso de la mascota"));
        verify(dosisCalculatorService, never()).calcularDosisTotal(any(), any());
        verify(tratamientoRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-23: Prescripción con consulta inexistente lanza ResourceNotFoundException (HTTP 404)")
    void prescribirTratamiento_ConsultaInexistente_LanzaResourceNotFound() {
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("10.00"),
                5,
                "Cada 8 horas"
        );

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tratamientoService.prescribirTratamiento(consultaId, request)
        );
    }

    @Test
    @DisplayName("RF-23: Prescripción con medicamento inexistente lanza ResourceNotFoundException (HTTP 404)")
    void prescribirTratamiento_MedicamentoInexistente_LanzaResourceNotFound() {
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("10.00"),
                5,
                "Cada 8 horas"
        );

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaConPeso));
        when(medicamentoRepository.findById(medicamentoId)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tratamientoService.prescribirTratamiento(consultaId, request)
        );
    }

    @Test
    @DisplayName("RF-23: Prescripción con medicamento inactivo lanza IllegalArgumentException")
    void prescribirTratamiento_MedicamentoInactivo_LanzaIllegalArgumentException() {
        medicamentoValido.setActivo(false);
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("10.00"),
                5,
                "Cada 8 horas"
        );

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaConPeso));
        when(medicamentoRepository.findById(medicamentoId)).thenReturn(Optional.of(medicamentoValido));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> tratamientoService.prescribirTratamiento(consultaId, request)
        );
        assertTrue(ex.getMessage().contains("se encuentra inactivo"));
    }

    @Test
    @DisplayName("RF-23: Listar tratamientos de consulta retorna historial con dosis calculadas")
    void listarPorConsulta_ConsultaExistente_RetornaLista() {
        // Arrange
        Tratamiento t1 = new Tratamiento(
                tratamientoId,
                consultaConPeso,
                medicamentoValido,
                new BigDecimal("10.00"),
                new BigDecimal("285.00"),
                5,
                "Cada 12 horas"
        );
        t1.setCreatedAt(LocalDateTime.now());

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consultaConPeso));
        when(tratamientoRepository.findByConsultaIdOrderByCreatedAtAsc(consultaId)).thenReturn(List.of(t1));

        // Act
        List<TratamientoResponse> lista = tratamientoService.listarPorConsulta(consultaId);

        // Assert
        assertEquals(1, lista.size());
        assertEquals(new BigDecimal("285.00"), lista.get(0).getDosisTotalMg());
        assertEquals("Amoxicilina + Clavulánico 250mg", lista.get(0).getMedicamentoNombre());
    }
}
