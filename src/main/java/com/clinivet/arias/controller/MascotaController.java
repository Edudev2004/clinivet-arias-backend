package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;
import com.clinivet.arias.service.IMascotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para la gestión de pacientes y mascotas de la clínica veterinaria.
 * Aplica principios SOLID:
 * - SRP: Encargado exclusivamente de orquestar peticiones y respuestas HTTP para mascotas.
 * - DIP: Depende de la abstracción IMascotaService mediante inyección por constructor.
 */
@RestController
@RequestMapping("/api/mascotas")
@Tag(name = "Mascotas", description = "Gestión de pacientes y expedientes veterinarios vinculados a propietarios")
public class MascotaController {

    private final IMascotaService mascotaService;

    public MascotaController(IMascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @PostMapping
    @Operation(summary = "Registrar nueva mascota (RF-09)",
            description = "Registra un nuevo paciente vinculándolo obligatoriamente a un propietario existente en BD (RN-01).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mascota registrada exitosamente",
                    content = @Content(schema = @Schema(implementation = MascotaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o propietario no existe (RN-01)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<MascotaResponse> registrar(@Valid @RequestBody MascotaCreateRequest request) {
        MascotaResponse response = mascotaService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar ficha completa de mascota (RF-10)",
            description = "Retorna la información completa de la mascota incluyendo el peso más reciente registrado en consultas médicas y datos de su propietario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ficha de mascota obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = MascotaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota no encontrada con el ID indicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<MascotaResponse> obtenerPorId(@PathVariable UUID id) {
        MascotaResponse response = mascotaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de una mascota (RF-11)",
            description = "Modifica los datos de una mascota registrada manteniendo el vínculo permanente con su propietario original.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mascota actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = MascotaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota no encontrada con el ID indicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<MascotaResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody MascotaUpdateRequest request) {
        MascotaResponse response = mascotaService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }
}
