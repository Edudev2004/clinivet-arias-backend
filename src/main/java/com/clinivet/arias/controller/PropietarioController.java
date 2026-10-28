package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.service.IPropietarioService;
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
 * Controlador REST para la gestión de propietarios (clientes).
 * Aplica principios SOLID:
 * - SRP: Encargado exclusivamente de orquestar peticiones y respuestas HTTP.
 * - DIP: Depende de la abstracción IPropietarioService mediante inyección por constructor.
 */
@RestController
@RequestMapping({"/api/propietarios", "/propietarios"})
@Tag(name = "Propietarios", description = "Endpoints para registro, consulta y administración de propietarios de mascotas")
public class PropietarioController {

    private final IPropietarioService propietarioService;

    public PropietarioController(IPropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo propietario (RF-05)",
            description = "Registra un cliente en el sistema. Valida unicidad de DNI y formato de correo electrónico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Propietario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PropietarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o campos obligatorios faltantes",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: Ya existe un propietario registrado con el mismo DNI",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PropietarioResponse> registrar(@Valid @RequestBody PropietarioCreateRequest request) {
        PropietarioResponse response = propietarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener propietario por ID",
            description = "Consulta la información detallada de un propietario a partir de su UUID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario encontrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PropietarioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado con el ID proporcionado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PropietarioResponse> obtenerPorId(@PathVariable UUID id) {
        PropietarioResponse response = propietarioService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }
}
