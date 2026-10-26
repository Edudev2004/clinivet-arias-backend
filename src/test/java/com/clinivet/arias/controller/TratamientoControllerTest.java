package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.CalculoDosisRequest;
import com.clinivet.arias.dto.CalculoDosisResponse;
import com.clinivet.arias.dto.TratamientoCreateRequest;
import com.clinivet.arias.dto.TratamientoResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.service.IDosisCalculatorService;
import com.clinivet.arias.service.ITratamientoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de capa Web para {@link TratamientoController} con MockMvc standalone.
 * Valida códigos de estado HTTP y estructura JSON según testing-rules.md (RF-23).
 */
@ExtendWith(MockitoExtension.class)
class TratamientoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IDosisCalculatorService dosisCalculatorService;

    @Mock
    private ITratamientoService tratamientoService;

    @InjectMocks
    private TratamientoController tratamientoController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID consultaId = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final UUID medicamentoId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private final UUID tratamientoId = UUID.fromString("f0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tratamientoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/tratamientos/calcular-dosis retorna 200 OK con dosis calculada (RN-12 / RF-23)")
    void calcularDosis_ValoresValidos_Retorna200Ok() throws Exception {
        // Arrange
        CalculoDosisRequest request = new CalculoDosisRequest(new BigDecimal("28.50"), new BigDecimal("12.50"));
        CalculoDosisResponse response = new CalculoDosisResponse(
                new BigDecimal("28.50"),
                new BigDecimal("12.50"),
                new BigDecimal("356.25")
        );

        when(dosisCalculatorService.calcular(any(CalculoDosisRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/tratamientos/calcular-dosis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesoKg").value(28.50))
                .andExpect(jsonPath("$.dosisMgPorKg").value(12.50))
                .andExpect(jsonPath("$.dosisTotalMg").value(356.25))
                .andExpect(jsonPath("$.unidadMedida").value("mg"));
    }

    @Test
    @DisplayName("POST /api/tratamientos/calcular-dosis retorna 400 Bad Request cuando el peso es inválido")
    void calcularDosis_PesoInvalido_Retorna400() throws Exception {
        CalculoDosisRequest request = new CalculoDosisRequest(new BigDecimal("-5.00"), new BigDecimal("10.00"));

        mockMvc.perform(post("/api/tratamientos/calcular-dosis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/consultas/{id}/tratamientos retorna 201 Created con tratamiento registrado y dosis (RF-23)")
    void prescribirTratamiento_ConsultaConPeso_Retorna201Created() throws Exception {
        // Arrange
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("12.50"),
                7,
                "Cada 12 horas con comida"
        );

        TratamientoResponse response = new TratamientoResponse(
                tratamientoId,
                consultaId,
                medicamentoId,
                "Amoxicilina + Clavulánico 250mg",
                "Amoxicilina / Ác. Clavulánico",
                new BigDecimal("28.50"),
                new BigDecimal("12.50"),
                new BigDecimal("356.25"),
                7,
                "Cada 12 horas con comida",
                LocalDateTime.now()
        );

        when(tratamientoService.prescribirTratamiento(eq(consultaId), any(TratamientoCreateRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/consultas/" + consultaId + "/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(tratamientoId.toString()))
                .andExpect(jsonPath("$.consultaId").value(consultaId.toString()))
                .andExpect(jsonPath("$.medicamentoNombre").value("Amoxicilina + Clavulánico 250mg"))
                .andExpect(jsonPath("$.pesoKgAplicado").value(28.50))
                .andExpect(jsonPath("$.dosisTotalMg").value(356.25));
    }

    @Test
    @DisplayName("POST /api/consultas/{id}/tratamientos retorna 400 Bad Request cuando la consulta no tiene peso (RF-23)")
    void prescribirTratamiento_SinPesoEnConsulta_Retorna400() throws Exception {
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("10.00"),
                5,
                "Cada 8 horas"
        );

        when(tratamientoService.prescribirTratamiento(eq(consultaId), any(TratamientoCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("La consulta médica no tiene registrado el peso de la mascota"));

        mockMvc.perform(post("/api/consultas/" + consultaId + "/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("La consulta médica no tiene registrado el peso de la mascota"));
    }

    @Test
    @DisplayName("POST /api/consultas/{id}/tratamientos retorna 404 Not Found cuando la consulta no existe")
    void prescribirTratamiento_ConsultaInexistente_Retorna404() throws Exception {
        TratamientoCreateRequest request = new TratamientoCreateRequest(
                medicamentoId,
                new BigDecimal("10.00"),
                5,
                "Cada 8 horas"
        );

        when(tratamientoService.prescribirTratamiento(eq(consultaId), any(TratamientoCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Consulta médica no encontrada con ID: " + consultaId));

        mockMvc.perform(post("/api/consultas/" + consultaId + "/tratamientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/consultas/{id}/tratamientos retorna 200 OK con lista de tratamientos")
    void listarTratamientos_Retorna200Ok() throws Exception {
        TratamientoResponse response = new TratamientoResponse(
                tratamientoId,
                consultaId,
                medicamentoId,
                "Amoxicilina + Clavulánico 250mg",
                "Amoxicilina / Ác. Clavulánico",
                new BigDecimal("28.50"),
                new BigDecimal("12.50"),
                new BigDecimal("356.25"),
                7,
                "Cada 12 horas",
                LocalDateTime.now()
        );

        when(tratamientoService.listarPorConsulta(consultaId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/consultas/" + consultaId + "/tratamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].dosisTotalMg").value(356.25))
                .andExpect(jsonPath("$[0].medicamentoNombre").value("Amoxicilina + Clavulánico 250mg"));
    }
}
