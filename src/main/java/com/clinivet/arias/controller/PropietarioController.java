package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.dto.PropietarioUpdateRequest;
import com.clinivet.arias.service.IPropietarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping
    @Operation(summary = "Búsqueda y listado paginado de propietarios (RF-06)",
            description = "Busca propietarios por nombre, apellido, DNI, teléfono o UUID. Retorna lista paginada con conteo de mascotas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados encontrados (lista vacía si no hay coincidencias)")
    })
    public ResponseEntity<Page<PropietarioResponse>> buscar(
            @RequestParam(name = "q", required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PropietarioResponse> resultados = propietarioService.buscar(q, pageable);
        return ResponseEntity.ok(resultados);
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

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de propietario (RF-07)",
            description = "Modifica los datos del propietario. Si cambia el DNI, valida que no pertenezca a otro propietario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propietario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PropietarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o incompletos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado con el ID indicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto: El DNI nuevo ya está registrado por otro propietario",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PropietarioResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PropietarioUpdateRequest request) {
        PropietarioResponse response = propietarioService.actualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/mascotas")
    @Operation(summary = "Consultar mascotas por propietario (RF-08)",
            description = "Retorna la lista de mascotas registradas asociadas a un propietario específico. Si el propietario no tiene mascotas, retorna una lista vacía con HTTP 200.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mascotas obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Propietario no encontrado con el ID indicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<MascotaResponse>> listarMascotas(@PathVariable UUID id) {
        List<MascotaResponse> mascotas = propietarioService.listarMascotasPorPropietario(id);
        return ResponseEntity.ok(mascotas);
    }
}
