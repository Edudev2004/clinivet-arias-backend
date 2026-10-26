package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.CitaRequest;
import com.clinivet.arias.dto.CitaResponse;
import com.clinivet.arias.exception.HorarioOcupadoException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.service.ICitaService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de capa Web para {@link CitaController} con MockMvc.
 * Valida los códigos HTTP, deserialización de fechas y manejo de errores estandarizado (RF-13).
 */
@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICitaService citaService;

    @InjectMocks
    private CitaController citaController;

    private ObjectMapper objectMapper;

    private final UUID citaId = UUID.fromString("70000000-0000-0000-0000-000000000001");
    private final UUID mascId = UUID.fromString("f0000000-0000-0000-0000-000000000001");
    private final UUID propId = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final UUID vetId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
    private final UUID adminId = UUID.fromString("b0000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(citaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private CitaResponse buildSampleResponse() {
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        return new CitaResponse(
                citaId,
                mascId,
                "Bobby",
                "Canino",
                propId,
                "Carlos Mendoza Ramos",
                "45892314",
                "987123456",
                vetId,
                "Beckhan Elias",
                "Medicina General y Cirugía Menor",
                fecha,
                hora,
                fechaHora,
                "PROGRAMADA",
                "Control y vacunación periódica",
                adminId,
                "admin@clinivet.pe",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/citas retorna 201 Created y cuerpo de respuesta con cita agendada")
    void registrarCita_PeticionValida_Retorna201Created() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, hora, "Control y vacunación periódica");
        CitaResponse response = buildSampleResponse();

        when(citaService.registrarCita(any(CitaRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(citaId.toString()))
                .andExpect(jsonPath("$.mascotaId").value(mascId.toString()))
                .andExpect(jsonPath("$.nombreMascota").value("Bobby"))
                .andExpect(jsonPath("$.nombrePropietario").value("Carlos Mendoza Ramos"))
                .andExpect(jsonPath("$.nombreVeterinario").value("Beckhan Elias"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.motivo").value("Control y vacunación periódica"));
    }

    @Test
    @DisplayName("POST /citas retorna 201 Created cumpliendo alias de ruta de APF2")
    void registrarCita_RutaAliasCitas_Retorna201Created() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(3);
        LocalTime hora = LocalTime.of(15, 0);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, hora, "Consulta dermatológica");
        CitaResponse response = buildSampleResponse();

        when(citaService.registrarCita(any(CitaRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(citaId.toString()));
    }

    @Test
    @DisplayName("POST /api/citas con campos obligatorios nulos retorna 400 Bad Request")
    void registrarCita_CamposNulos_Retorna400BadRequest() throws Exception {
        CitaRequest requestInvalido = new CitaRequest(null, null, null, null, null, "");

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/citas cuando la mascota no existe retorna 404 Not Found")
    void registrarCita_MascotaInexistente_Retorna404NotFound() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(10, 0), "Motivo");

        when(citaService.registrarCita(any(CitaRequest.class), any()))
                .thenThrow(new ResourceNotFoundException("Mascota no encontrada con ID: " + mascId));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Mascota no encontrada con ID: " + mascId));
    }

    @Test
    @DisplayName("POST /api/citas con mascota que no pertenece al propietario retorna 400 Bad Request")
    void registrarCita_MascotaNoEsDelPropietario_Retorna400BadRequest() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(1);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, LocalTime.of(10, 0), "Motivo");

        when(citaService.registrarCita(any(CitaRequest.class), any()))
                .thenThrow(new IllegalArgumentException("La mascota especificada no pertenece al propietario indicado"));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("La mascota especificada no pertenece al propietario indicado"));
    }

    @Test
    @DisplayName("GET /api/citas/{id} retorna 200 OK con los datos de la cita")
    void obtenerPorId_CitaExiste_Retorna200Ok() throws Exception {
        CitaResponse response = buildSampleResponse();
        when(citaService.obtenerCitaPorId(eq(citaId))).thenReturn(response);

        mockMvc.perform(get("/api/citas/{id}", citaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaId.toString()))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.nombreMascota").value("Bobby"));
    }

    @Test
    @DisplayName("GET /api/citas/{id} con ID no existente retorna 404 Not Found")
    void obtenerPorId_CitaInexistente_Retorna404NotFound() throws Exception {
        when(citaService.obtenerCitaPorId(eq(citaId)))
                .thenThrow(new ResourceNotFoundException("Cita médica no encontrada con ID: " + citaId));

        mockMvc.perform(get("/api/citas/{id}", citaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/citas con cruce de horario para el veterinario retorna 409 Conflict (RF-15)")
    void registrarCita_ConflictoHorario_Retorna409Conflict() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        CitaRequest request = new CitaRequest(mascId, propId, vetId, fecha, hora, "Motivo");

        when(citaService.registrarCita(any(CitaRequest.class), any()))
                .thenThrow(new HorarioOcupadoException("El veterinario ya tiene una cita registrada en esa fecha y hora"));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("El veterinario ya tiene una cita registrada en esa fecha y hora"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/reprogramar retorna 200 OK con cita reprogramada (RF-16)")
    void reprogramarCita_PeticionValida_Retorna200Ok() throws Exception {
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        LocalTime nuevaHora = LocalTime.of(16, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);
        CitaResponse response = buildSampleResponse();

        when(citaService.reprogramarCita(eq(citaId), any(com.clinivet.arias.dto.CitaReprogramarRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/citas/{id}/reprogramar", citaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaId.toString()))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("PATCH /citas/{id}/reprogramar alias de ruta retorna 200 OK (RF-16)")
    void reprogramarCita_RutaAlias_Retorna200Ok() throws Exception {
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        LocalTime nuevaHora = LocalTime.of(16, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);
        CitaResponse response = buildSampleResponse();

        when(citaService.reprogramarCita(eq(citaId), any(com.clinivet.arias.dto.CitaReprogramarRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/citas/{id}/reprogramar", citaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaId.toString()));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/reprogramar con conflicto horario retorna 409 Conflict (RF-16)")
    void reprogramarCita_ConflictoHorario_Retorna409Conflict() throws Exception {
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        LocalTime nuevaHora = LocalTime.of(16, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);

        when(citaService.reprogramarCita(eq(citaId), any(com.clinivet.arias.dto.CitaReprogramarRequest.class)))
                .thenThrow(new HorarioOcupadoException("El veterinario ya tiene una cita registrada en esa fecha y hora"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/citas/{id}/reprogramar", citaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("El veterinario ya tiene una cita registrada en esa fecha y hora"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/reprogramar en estado no válido retorna 400 Bad Request (RF-16)")
    void reprogramarCita_EstadoInvalido_Retorna400BadRequest() throws Exception {
        LocalDate nuevaFecha = LocalDate.now().plusDays(5);
        LocalTime nuevaHora = LocalTime.of(16, 0);
        com.clinivet.arias.dto.CitaReprogramarRequest request = new com.clinivet.arias.dto.CitaReprogramarRequest(nuevaFecha, nuevaHora);

        when(citaService.reprogramarCita(eq(citaId), any(com.clinivet.arias.dto.CitaReprogramarRequest.class)))
                .thenThrow(new IllegalStateException("Solo se pueden reprogramar citas con estado PROGRAMADA"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/citas/{id}/reprogramar", citaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("Solo se pueden reprogramar citas con estado PROGRAMADA"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/cancelar retorna 200 OK con estado CANCELADA (RF-17)")
    void cancelarCita_PeticionValida_Retorna200Ok() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        CitaResponse response = new CitaResponse(
                citaId, mascId, "Bobby", "Canino", propId, "Carlos Mendoza", "45892314", "987123456",
                vetId, "Beckhan Elias", "Medicina General", fecha, hora, fechaHora, "CANCELADA",
                "Motivo", adminId, "admin@clinivet.pe", LocalDateTime.now()
        );

        when(citaService.cancelarCita(eq(citaId))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/citas/{id}/cancelar", citaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaId.toString()))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("PATCH /citas/{id}/cancelar alias de ruta retorna 200 OK (RF-17)")
    void cancelarCita_RutaAlias_Retorna200Ok() throws Exception {
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(10, 30);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        CitaResponse response = new CitaResponse(
                citaId, mascId, "Bobby", "Canino", propId, "Carlos Mendoza", "45892314", "987123456",
                vetId, "Beckhan Elias", "Medicina General", fecha, hora, fechaHora, "CANCELADA",
                "Motivo", adminId, "admin@clinivet.pe", LocalDateTime.now()
        );

        when(citaService.cancelarCita(eq(citaId))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/citas/{id}/cancelar", citaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaId.toString()))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/cancelar con cita ya cancelada retorna 400 Bad Request (RF-17)")
    void cancelarCita_CitaYaCancelada_Retorna400BadRequest() throws Exception {
        when(citaService.cancelarCita(eq(citaId)))
                .thenThrow(new IllegalStateException("La cita ya se encuentra cancelada"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/citas/{id}/cancelar", citaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("La cita ya se encuentra cancelada"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/cancelar con cita ya atendida retorna 400 Bad Request (RF-17)")
    void cancelarCita_CitaYaAtendida_Retorna400BadRequest() throws Exception {
        when(citaService.cancelarCita(eq(citaId)))
                .thenThrow(new IllegalStateException("No se puede cancelar una cita que ya fue atendida"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/citas/{id}/cancelar", citaId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("No se puede cancelar una cita que ya fue atendida"));
    }
}
