package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.DisponibilidadResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.service.IVeterinarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de capa Web para {@link VeterinarioController} con MockMvc standalone.
 * Valida códigos HTTP y serialización JSON para RF-14 según testing-rules.md.
 */
@ExtendWith(MockitoExtension.class)
class VeterinarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IVeterinarioService veterinarioService;

    @InjectMocks
    private VeterinarioController veterinarioController;

    private final UUID vetId = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private final LocalDate fecha = LocalDate.of(2026, 11, 2);
    private final LocalTime hora = LocalTime.of(10, 0, 0);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(veterinarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/veterinarios/{id}/disponibilidad retorna 200 OK con respuesta estructurada")
    void consultarDisponibilidad_PeticionValida_Retorna200Ok() throws Exception {
        DisponibilidadResponse response = new DisponibilidadResponse(
                vetId,
                "Dr. Beckhan Elias",
                "CMVP-14892",
                "Medicina General y Cirugía Menor",
                fecha,
                hora,
                true,
                "Horario disponible para agendar atención médica",
                List.of("09:00 - 13:00", "14:00 - 18:00")
        );

        when(veterinarioService.consultarDisponibilidad(eq(vetId), eq(fecha), eq(hora)))
                .thenReturn(response);

        mockMvc.perform(get("/api/veterinarios/" + vetId + "/disponibilidad")
                        .param("fecha", "2026-11-02")
                        .param("hora", "10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veterinarioId").value(vetId.toString()))
                .andExpect(jsonPath("$.veterinarioNombre").value("Dr. Beckhan Elias"))
                .andExpect(jsonPath("$.numeroColegiatura").value("CMVP-14892"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.fecha").value("2026-11-02"))
                .andExpect(jsonPath("$.hora").value("10:00:00"));
    }

    @Test
    @DisplayName("GET /api/veterinarios/{id}/disponibilidad retorna 404 cuando el veterinario no existe")
    void consultarDisponibilidad_VeterinarioInexistente_Retorna404() throws Exception {
        when(veterinarioService.consultarDisponibilidad(eq(vetId), eq(fecha), eq(hora)))
                .thenThrow(new ResourceNotFoundException("Médico veterinario no encontrado con ID: " + vetId));

        mockMvc.perform(get("/api/veterinarios/" + vetId + "/disponibilidad")
                        .param("fecha", "2026-11-02")
                        .param("hora", "10:00:00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Médico veterinario no encontrado con ID: " + vetId));
    }

    @Test
    @DisplayName("GET /api/veterinarios/{id}/disponibilidad retorna 400 cuando falta parámetro requerido")
    void consultarDisponibilidad_ParametroFaltante_Retorna400() throws Exception {
        // Falta parámetro 'hora'
        mockMvc.perform(get("/api/veterinarios/" + vetId + "/disponibilidad")
                        .param("fecha", "2026-11-02"))
                .andExpect(status().isBadRequest());
    }
}
