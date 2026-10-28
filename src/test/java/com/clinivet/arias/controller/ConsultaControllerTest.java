package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;
import com.clinivet.arias.exception.ConsultaDuplicadaException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.service.IConsultaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de capa web para {@link ConsultaController} (RF-12) con MockMvc standalone.
 */
@ExtendWith(MockitoExtension.class)
class ConsultaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IConsultaService consultaService;

    @InjectMocks
    private ConsultaController consultaController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final UUID citaId = UUID.fromString("c0000000-0000-0000-0000-000000000001");
    private final UUID veterinarioId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private final UUID mascotaId = UUID.fromString("f0000000-0000-0000-0000-000000000001");
    private final UUID consultaId = UUID.fromString("e0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(consultaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ConsultaRequest requestValido() {
        return new ConsultaRequest(citaId, veterinarioId, LocalDate.of(2026, 11, 15),
                "Control post-operatorio", "Herida en cicatrización adecuada",
                new BigDecimal("28.50"), new BigDecimal("38.5"), "Sin complicaciones");
    }

    @Test
    @DisplayName("POST /api/consultas retorna 201 con la consulta creada y su ID")
    void registrarConsulta_Retorna201() throws Exception {
        ConsultaResponse response = new ConsultaResponse(consultaId, citaId, mascotaId, "Firulais", veterinarioId,
                LocalDate.of(2026, 11, 15), "Control post-operatorio", "Herida en cicatrización adecuada",
                new BigDecimal("28.50"), new BigDecimal("38.5"), "Sin complicaciones", LocalDateTime.now());
        when(consultaService.registrarConsulta(any(ConsultaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(consultaId.toString()))
                .andExpect(jsonPath("$.citaId").value(citaId.toString()))
                .andExpect(jsonPath("$.mascotaId").value(mascotaId.toString()))
                .andExpect(jsonPath("$.pesoKg").value(28.50))
                .andExpect(jsonPath("$.temperaturaC").value(38.5));
    }

    @Test
    @DisplayName("POST /api/consultas retorna 400 cuando faltan campos obligatorios")
    void registrarConsulta_CamposFaltantes_Retorna400() throws Exception {
        ConsultaRequest request = requestValido();
        request.setCitaId(null);
        request.setPeso(null);
        request.setMotivo(" ");

        mockMvc.perform(post("/api/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(consultaService, never()).registrarConsulta(any());
    }

    @Test
    @DisplayName("POST /api/consultas retorna 404 cuando la cita no existe")
    void registrarConsulta_CitaInvalida_Retorna404() throws Exception {
        when(consultaService.registrarConsulta(any(ConsultaRequest.class)))
                .thenThrow(new ResourceNotFoundException("Cita médica no encontrada con ID: " + citaId));

        mockMvc.perform(post("/api/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/consultas retorna 409 cuando la cita ya tiene consulta")
    void registrarConsulta_CitaYaAtendida_Retorna409() throws Exception {
        when(consultaService.registrarConsulta(any(ConsultaRequest.class)))
                .thenThrow(new ConsultaDuplicadaException("La cita ya tiene una consulta registrada"));

        mockMvc.perform(post("/api/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
