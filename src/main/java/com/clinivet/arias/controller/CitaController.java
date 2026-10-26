package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.CitaRequest;
import com.clinivet.arias.dto.CitaResponse;
import com.clinivet.arias.service.ICitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para la gestión de citas médicas (RF-13 a RF-19).
 * Cumple con SRP (solo orquesta solicitudes HTTP) y DIP (inyección por constructor de ICitaService).
 */
@RestController
@RequestMapping({"/api/citas", "/citas"})
@Tag(name = "Citas Médicas", description = "Endpoints para el agendamiento, consulta y ciclo de vida de citas médicas (APF2 Épica 3)")
public class CitaController {

    private final ICitaService citaService;

    public CitaController(ICitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    @Operation(summary = "Registrar nueva cita médica",
            description = "Agenda una cita médica asociándola a una mascota, propietario y veterinario. El estado inicial es siempre PROGRAMADA (RF-13).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cita médica agendada exitosamente",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos, campos faltantes o fecha en el pasado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota, propietario o veterinario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Cruce de horario para el veterinario en la fecha y hora seleccionada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CitaResponse> registrarCita(@Valid @RequestBody CitaRequest request,
                                                      Authentication authentication) {
        String emailUsuario = authentication != null ? authentication.getName() : null;
        CitaResponse response = citaService.registrarCita(request, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar citas médicas con filtros dinámicos y paginación",
            description = "Consulta la lista paginada de citas médicas permitiendo filtrar opcionalmente por veterinario, fecha puntual, rango de fechas y estado (RF-18). Siempre ordenado por fecha y hora ascendente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado paginado de citas obtenido exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos o estado no reconocido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<org.springframework.data.domain.Page<CitaResponse>> listarCitas(
            @RequestParam(name = "veterinarioId", required = false) UUID veterinarioId,
            @RequestParam(name = "fecha", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha,
            @RequestParam(name = "fechaInicio", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaFin,
            @RequestParam(name = "estado", required = false) String estado,
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<CitaResponse> response =
                citaService.listarCitasConFiltros(veterinarioId, fecha, fechaInicio, fechaFin, estado, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar cita por identificador único (UUID)",
            description = "Retorna el detalle completo y estado de la cita médica.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita encontrada con éxito",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada con el ID proporcionado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CitaResponse> obtenerPorId(@PathVariable("id") UUID id) {
        CitaResponse response = citaService.obtenerCitaPorId(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reprogramar")
    @Operation(summary = "Reprogramar cita médica a nueva fecha y hora",
            description = "Actualiza la fecha y hora de una cita en estado PROGRAMADA validando la disponibilidad de agenda del veterinario (RF-16).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita médica reprogramada exitosamente",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado no válido para reprogramar o fecha pasada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada con el ID indicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de horario con otra cita programada para el veterinario",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CitaResponse> reprogramarCita(@PathVariable("id") UUID id,
                                                        @Valid @RequestBody com.clinivet.arias.dto.CitaReprogramarRequest request) {
        CitaResponse response = citaService.reprogramarCita(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar cita médica conservando el registro histórico",
            description = "Cambia el estado de una cita a CANCELADA sin eliminarla de la base de datos (RF-17 / RN-07).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cita médica cancelada exitosamente",
                    content = @Content(schema = @Schema(implementation = CitaResponse.class))),
            @ApiResponse(responseCode = "400", description = "La cita ya está cancelada o atendida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Cita no encontrada con el ID indicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CitaResponse> cancelarCita(@PathVariable("id") UUID id) {
        CitaResponse response = citaService.cancelarCita(id);
        return ResponseEntity.ok(response);
    }
}
