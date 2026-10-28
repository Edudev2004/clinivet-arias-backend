package com.clinivet.arias.controller;

import com.clinivet.arias.dto.ApiErrorResponse;
import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;
import com.clinivet.arias.dto.VacunaAplicadaRequest;
import com.clinivet.arias.dto.VacunaAplicadaResponse;
import com.clinivet.arias.service.IMascotaService;
import com.clinivet.arias.service.IVacunaService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de pacientes, expedientes veterinarios y vacunación.
 * Aplica principios SOLID:
 * - SRP: Encargado exclusivamente de orquestar peticiones y respuestas HTTP.
 * - DIP: Depende de abstracciones (IMascotaService, IVacunaService) mediante inyección por constructor.
 */
@RestController
@RequestMapping("/api/mascotas")
@Tag(name = "Mascotas", description = "Gestión de pacientes, expedientes veterinarios y vacunación vinculados a propietarios")
public class MascotaController {

    private final IMascotaService mascotaService;
    private final IVacunaService vacunaService;

    public MascotaController(IMascotaService mascotaService, IVacunaService vacunaService) {
        this.mascotaService = mascotaService;
        this.vacunaService = vacunaService;
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

    @GetMapping
    @Operation(summary = "Listar todas las mascotas activas (RF-10 / RF-12)",
            description = "Retorna el listado completo de pacientes y mascotas activas con su peso más reciente registrado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de mascotas obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = MascotaResponse.class)))
    })
    public ResponseEntity<List<MascotaResponse>> listarTodas() {
        List<MascotaResponse> response = mascotaService.listarTodas();
        return ResponseEntity.ok(response);
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

    @PostMapping("/{id}/vacunas")
    @PreAuthorize("hasRole('VETERINARIO')") // RN-04: solo veterinarios aplican vacunas
    @Operation(summary = "Registrar vacuna aplicada a una mascota (RF-24)",
            description = "Registra una vacuna administrada al paciente con su fecha y lote opcional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vacuna aplicada registrada exitosamente",
                    content = @Content(schema = @Schema(implementation = VacunaAplicadaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol VETERINARIO (RN-04)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota o veterinario no encontrados",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VacunaAplicadaResponse> registrarVacuna(
            @PathVariable UUID id,
            @Valid @RequestBody VacunaAplicadaRequest request) {
        VacunaAplicadaResponse response = vacunaService.registrarVacuna(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/vacunas")
    @Operation(summary = "Consultar historial de vacunas de una mascota (RF-24)",
            description = "Retorna el historial completo de vacunación de la mascota ordenado cronológicamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial de vacunas obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = VacunaAplicadaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<VacunaAplicadaResponse>> listarVacunas(@PathVariable UUID id) {
        List<VacunaAplicadaResponse> response = vacunaService.listarPorMascota(id);
        return ResponseEntity.ok(response);
    }
}
