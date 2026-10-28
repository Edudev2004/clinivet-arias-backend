package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
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
}
