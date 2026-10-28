package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.service.IMascotaService;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de capa Web para {@link MascotaController} con MockMvc.
 * Valida códigos HTTP y serialización JSON para:
 * - RF-09: Registro exitoso (201), validación de campos obligatorios (400) y propietario inexistente (400 / RN-01).
 * - RF-10: Consulta de ficha completa (200, 404).
 * - RF-11: Edición de datos de mascota (200, 400, 404).
 */
@ExtendWith(MockitoExtension.class)
class MascotaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IMascotaService mascotaService;

    @InjectMocks
    private MascotaController mascotaController;

    private ObjectMapper objectMapper;

    private final UUID PROPIETARIO_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private final UUID MASCOTA_ID = UUID.fromString("f0000000-0000-0000-0000-000000000001");
    private MascotaResponse responseValida;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(mascotaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        responseValida = new MascotaResponse(
                MASCOTA_ID,
                "Firulais",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("28.50"),
                "https://storage.supabase.co/clinivet/photos/firulais.jpg",
                true,
                PROPIETARIO_ID,
                "Carlos Mendoza Ramos",
                LocalDateTime.of(2026, 10, 27, 10, 0)
        );
    }

    @Test
    @DisplayName("POST /api/mascotas: Retorna 201 Created cuando los datos son válidos y existe el propietario (RF-09)")
    void registrar_retorna201() throws Exception {
        MascotaCreateRequest request = new MascotaCreateRequest(
                PROPIETARIO_ID,
                "Firulais",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("28.50"),
                "https://storage.supabase.co/clinivet/photos/firulais.jpg"
        );

        when(mascotaService.registrar(any(MascotaCreateRequest.class))).thenReturn(responseValida);

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MASCOTA_ID.toString()))
                .andExpect(jsonPath("$.nombre").value("Firulais"))
                .andExpect(jsonPath("$.especie").value("Canino"))
                .andExpect(jsonPath("$.raza").value("Golden Retriever"))
                .andExpect(jsonPath("$.sexo").value("Macho"))
                .andExpect(jsonPath("$.fechaNacimiento").value("2022-03-10"))
                .andExpect(jsonPath("$.pesoActualKg").value(28.50))
                .andExpect(jsonPath("$.propietarioId").value(PROPIETARIO_ID.toString()))
                .andExpect(jsonPath("$.propietarioNombre").value("Carlos Mendoza Ramos"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/mascotas: Retorna 400 Bad Request cuando el propietario no existe en BD (RN-01)")
    void registrar_propietarioInexistente_retorna400() throws Exception {
        UUID propietarioInexistente = UUID.randomUUID();
        MascotaCreateRequest request = new MascotaCreateRequest(
                propietarioInexistente,
                "Bobby",
                "Canino",
                "Beagle",
                "Tricolor",
                "Macho",
                null,
                null,
                null
        );

        when(mascotaService.registrar(any(MascotaCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("El propietario con ID [" + propietarioInexistente + "] no existe en la base de datos (RN-01)"));

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("El propietario con ID [" + propietarioInexistente + "] no existe en la base de datos (RN-01)"));
    }

    @Test
    @DisplayName("POST /api/mascotas: Retorna 400 Bad Request cuando faltan campos obligatorios")
    void registrar_camposFaltantes_retorna400() throws Exception {
        MascotaCreateRequest requestInvalido = new MascotaCreateRequest(
                null, // propietarioId nulo (RN-01)
                "", // nombre vacío
                "", // especie vacía
                null,
                null,
                "Invalido", // sexo no permitido
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/mascotas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RF-10: PRUEBAS DE CONSULTA DE FICHA COMPLETA DE MASCOTA (GET /api/mascotas/{id})
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/mascotas/{id}: Retorna 200 OK con ficha completa y datos de propietario (RF-10)")
    void obtenerPorId_retorna200() throws Exception {
        when(mascotaService.obtenerPorId(MASCOTA_ID)).thenReturn(responseValida);

        mockMvc.perform(get("/api/mascotas/{id}", MASCOTA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MASCOTA_ID.toString()))
                .andExpect(jsonPath("$.nombre").value("Firulais"))
                .andExpect(jsonPath("$.especie").value("Canino"))
                .andExpect(jsonPath("$.raza").value("Golden Retriever"))
                .andExpect(jsonPath("$.sexo").value("Macho"))
                .andExpect(jsonPath("$.pesoActualKg").value(28.50))
                .andExpect(jsonPath("$.propietarioId").value(PROPIETARIO_ID.toString()))
                .andExpect(jsonPath("$.propietarioNombre").value("Carlos Mendoza Ramos"));
    }

    @Test
    @DisplayName("GET /api/mascotas/{id}: Retorna 404 Not Found cuando la mascota no existe (RF-10)")
    void obtenerPorId_noExiste_retorna404() throws Exception {
        UUID inexistente = UUID.randomUUID();
        when(mascotaService.obtenerPorId(inexistente))
                .thenThrow(new ResourceNotFoundException("Mascota no encontrada con ID: " + inexistente));

        mockMvc.perform(get("/api/mascotas/{id}", inexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Mascota no encontrada con ID: " + inexistente));
    }

    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/mascotas: Retorna 200 OK con listado de mascotas activas y peso vigente (RF-10 / RF-12)")
    void listarTodas_retorna200() throws Exception {
        when(mascotaService.listarTodas()).thenReturn(List.of(responseValida));

        mockMvc.perform(get("/api/mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(MASCOTA_ID.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Firulais"))
                .andExpect(jsonPath("$[0].pesoActualKg").value(28.50));
    }

    // RF-11: PRUEBAS DE ACTUALIZACIÓN DE MASCOTA (PUT /api/mascotas/{id})
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/mascotas/{id}: Retorna 200 OK cuando los datos son válidos y la mascota existe (RF-11)")
    void actualizar_retorna200() throws Exception {
        MascotaUpdateRequest request = new MascotaUpdateRequest(
                "Firulais Actualizado",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("29.00"),
                "https://storage.supabase.co/clinivet/photos/firulais_v2.jpg",
                true
        );

        MascotaResponse responseActualizada = new MascotaResponse(
                MASCOTA_ID,
                "Firulais Actualizado",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                LocalDate.of(2022, 3, 10),
                new BigDecimal("29.00"),
                "https://storage.supabase.co/clinivet/photos/firulais_v2.jpg",
                true,
                PROPIETARIO_ID,
                "Carlos Mendoza Ramos",
                LocalDateTime.of(2026, 10, 27, 10, 0)
        );

        when(mascotaService.actualizar(eq(MASCOTA_ID), any(MascotaUpdateRequest.class)))
                .thenReturn(responseActualizada);

        mockMvc.perform(put("/api/mascotas/{id}", MASCOTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MASCOTA_ID.toString()))
                .andExpect(jsonPath("$.nombre").value("Firulais Actualizado"))
                .andExpect(jsonPath("$.pesoActualKg").value(29.00))
                .andExpect(jsonPath("$.fotoUrl").value("https://storage.supabase.co/clinivet/photos/firulais_v2.jpg"))
                .andExpect(jsonPath("$.propietarioId").value(PROPIETARIO_ID.toString()))
                .andExpect(jsonPath("$.propietarioNombre").value("Carlos Mendoza Ramos"));
    }

    @Test
    @DisplayName("PUT /api/mascotas/{id}: Retorna 404 Not Found cuando la mascota a actualizar no existe (RF-11)")
    void actualizar_noExiste_retorna404() throws Exception {
        UUID inexistente = UUID.randomUUID();
        MascotaUpdateRequest request = new MascotaUpdateRequest(
                "Firulais Actualizado",
                "Canino",
                "Golden Retriever",
                "Dorado",
                "Macho",
                null,
                new BigDecimal("29.00"),
                null,
                true
        );

        when(mascotaService.actualizar(eq(inexistente), any(MascotaUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Mascota no encontrada con ID: " + inexistente));

        mockMvc.perform(put("/api/mascotas/{id}", inexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Mascota no encontrada con ID: " + inexistente));
    }

    @Test
    @DisplayName("PUT /api/mascotas/{id}: Retorna 400 Bad Request cuando los datos de actualización son inválidos (RF-11)")
    void actualizar_datosInvalidos_retorna400() throws Exception {
        MascotaUpdateRequest requestInvalido = new MascotaUpdateRequest(
                "", // nombre en blanco
                "", // especie en blanco
                null,
                null,
                "Invalido", // sexo inválido
                LocalDate.now().plusDays(5), // fecha futura no permitida
                new BigDecimal("-5.00"), // peso negativo no permitido
                null,
                null
        );

        mockMvc.perform(put("/api/mascotas/{id}", MASCOTA_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
