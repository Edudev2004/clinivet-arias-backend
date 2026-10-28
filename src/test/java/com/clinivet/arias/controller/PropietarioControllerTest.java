package com.clinivet.arias.controller;

import com.clinivet.arias.config.GlobalExceptionHandler;
import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.exception.DuplicateResourceException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.service.IPropietarioService;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias de capa Web para {@link PropietarioController} con MockMvc.
 * Valida códigos HTTP:
 * - 201 Created para registro exitoso (RF-05)
 * - 400 Bad Request para validación de datos faltantes/incorrectos
 * - 409 Conflict para DNI duplicado en el sistema
 * - 404 Not Found para propietario inexistente
 */
@ExtendWith(MockitoExtension.class)
class PropietarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IPropietarioService propietarioService;

    @InjectMocks
    private PropietarioController propietarioController;

    private ObjectMapper objectMapper;

    private final UUID PROPIETARIO_ID = UUID.fromString("e0000000-0000-0000-0000-000000000001");
    private PropietarioResponse responseValida;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(propietarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseValida = new PropietarioResponse(
                PROPIETARIO_ID,
                "Carlos",
                "Mendoza Ramos",
                "45871236",
                "987654321",
                "carlos.mendoza@gmail.com",
                "Av. Universitaria 1240, Carabayllo",
                true,
                LocalDateTime.of(2026, 10, 27, 10, 0)
        );
    }

    @Test
    @DisplayName("POST /api/propietarios: Retorna 201 Created cuando los datos son válidos")
    void registrar_retorna201() throws Exception {
        PropietarioCreateRequest request = new PropietarioCreateRequest(
                "Carlos",
                "Mendoza Ramos",
                "45871236",
                "987654321",
                "carlos.mendoza@gmail.com",
                "Av. Universitaria 1240, Carabayllo"
        );

        when(propietarioService.registrar(any(PropietarioCreateRequest.class))).thenReturn(responseValida);

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PROPIETARIO_ID.toString()))
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.apellido").value("Mendoza Ramos"))
                .andExpect(jsonPath("$.dni").value("45871236"))
                .andExpect(jsonPath("$.email").value("carlos.mendoza@gmail.com"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/propietarios: Retorna 409 Conflict cuando el DNI ya existe")
    void registrar_dniDuplicado_retorna409() throws Exception {
        PropietarioCreateRequest request = new PropietarioCreateRequest(
                "Carlos",
                "Mendoza Ramos",
                "45871236",
                "987654321",
                "carlos.mendoza@gmail.com",
                "Av. Universitaria 1240"
        );

        when(propietarioService.registrar(any(PropietarioCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Ya existe un propietario registrado con el DNI: 45871236"));

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un propietario registrado con el DNI: 45871236"));
    }

    @Test
    @DisplayName("POST /api/propietarios: Retorna 400 Bad Request cuando faltan campos obligatorios")
    void registrar_camposFaltantes_retorna400() throws Exception {
        PropietarioCreateRequest requestInvalido = new PropietarioCreateRequest(
                "", // nombre vacío
                "Mendoza",
                "123", // DNI muy corto
                "", // teléfono vacío
                "correo-invalido", // email inválido
                null
        );

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /api/propietarios/{id}: Retorna 200 OK cuando existe")
    void obtenerPorId_retorna200() throws Exception {
        when(propietarioService.obtenerPorId(PROPIETARIO_ID)).thenReturn(responseValida);

        mockMvc.perform(get("/api/propietarios/{id}", PROPIETARIO_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PROPIETARIO_ID.toString()))
                .andExpect(jsonPath("$.dni").value("45871236"));
    }

    @Test
    @DisplayName("GET /api/propietarios/{id}: Retorna 404 Not Found cuando no existe")
    void obtenerPorId_retorna404() throws Exception {
        UUID noExistente = UUID.randomUUID();
        when(propietarioService.obtenerPorId(noExistente))
                .thenThrow(new ResourceNotFoundException("Propietario no encontrado con ID: " + noExistente));

        mockMvc.perform(get("/api/propietarios/{id}", noExistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
