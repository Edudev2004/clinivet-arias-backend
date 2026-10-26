package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.DisponibilidadResponse;
import com.clinivet.arias.service.IVeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Controlador REST para médicos veterinarios y consulta de disponibilidad de agenda.
 * Cumple con SRP (orquestación HTTP), DIP (depende de IVeterinarioService) y el requerimiento RF-14.
 */
@RestController
@RequestMapping({"/api/veterinarios", "/veterinarios"})
@Tag(name = "Veterinarios y Agenda", description = "Endpoints para consulta de disponibilidad y turnos médicos (RF-14 / RN-03 / RN-11)")
public class VeterinarioController {

    private final IVeterinarioService veterinarioService;

    // Inyección de dependencias por constructor (DIP)
    public VeterinarioController(IVeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Consultar disponibilidad de un veterinario",
            description = "Valida si el especialista atiende en la fecha y hora indicadas y no cuenta con citas programadas que colisionen (RF-14)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta procesada exitosamente",
                    content = @Content(schema = @Schema(implementation = DisponibilidadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de fecha u hora faltantes o inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Médico veterinario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidad(
            @Parameter(description = "Identificador UUID del veterinario", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Fecha deseada en formato YYYY-MM-DD", example = "2026-11-02", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "Hora deseada en formato HH:mm:ss o HH:mm", example = "10:00:00", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        DisponibilidadResponse response = veterinarioService.consultarDisponibilidad(id, fecha, hora);
        return ResponseEntity.ok(response);
    }
}
