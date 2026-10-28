package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;
import com.clinivet.arias.service.IConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el registro de consultas médicas (RF-12).
 * Solo orquesta HTTP (SRP) e inyecta la interfaz de servicio (DIP).
 */
@RestController
@RequestMapping("/api/consultas")
@Tag(name = "Consultas Médicas", description = "Registro de consultas clínicas vinculadas a citas (RF-12)")
public class ConsultaController {

    private final IConsultaService consultaService;

    public ConsultaController(IConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('VETERINARIO')") // RN-04: solo veterinarios registran atención clínica
    @Operation(summary = "Registrar consulta médica",
            description = "Registra la consulta de una cita, marca la cita como ATENDIDA y actualiza el peso actual de la mascota (RF-12)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Consulta creada",
                    content = @Content(schema = @Schema(implementation = ConsultaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Campos faltantes/inválidos o cita no atendible",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol VETERINARIO",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita, veterinario o mascota no encontrados",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La cita ya tiene una consulta registrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<ConsultaResponse> registrarConsulta(@Valid @RequestBody ConsultaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.registrarConsulta(request));
    }
}
