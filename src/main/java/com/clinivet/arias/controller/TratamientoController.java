package com.clinivet.arias.controller;

import com.clinivet.arias.dto.*;
import com.clinivet.arias.service.IDosisCalculatorService;
import com.clinivet.arias.service.ITratamientoService;
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

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para prescripciones médicas y cálculo automático de dosis.
 * Cumple con SRP (exclusivamente orquestación HTTP), DIP (inyecta interfaces de servicio),
 * y los requerimientos RF-23 / RN-12.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Tratamientos y Prescripciones", description = "Endpoints para prescripción médica y cálculo automático de dosis (RF-23 / RN-12)")
public class TratamientoController {

    private final IDosisCalculatorService dosisCalculatorService;
    private final ITratamientoService tratamientoService;

    // Inyección de dependencias por constructor (DIP)
    public TratamientoController(IDosisCalculatorService dosisCalculatorService,
                                 ITratamientoService tratamientoService) {
        this.dosisCalculatorService = dosisCalculatorService;
        this.tratamientoService = tratamientoService;
    }

    @PostMapping("/tratamientos/calcular-dosis")
    @Operation(summary = "Calcular dosis total de medicamento",
            description = "Aplica la fórmula matemática DosisTotal = Peso * Dosis (mg/kg) para asistencia reactiva en consulta (RN-12 / RF-23)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cálculo exitoso",
                    content = @Content(schema = @Schema(implementation = CalculoDosisResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parámetros de peso o dosis inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<CalculoDosisResponse> calcularDosis(@Valid @RequestBody CalculoDosisRequest request) {
        CalculoDosisResponse response = dosisCalculatorService.calcular(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/consultas/{consultaId}/tratamientos")
    @Operation(summary = "Prescribir tratamiento con cálculo automático de dosis",
            description = "Registra un tratamiento en la consulta usando el peso registrado de la mascota para calcular la dosis total (RF-23)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tratamiento registrado exitosamente con dosis calculada",
                    content = @Content(schema = @Schema(implementation = TratamientoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Consulta sin peso registrado o dosis inválida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Consulta o medicamento no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TratamientoResponse> prescribirTratamiento(
            @PathVariable UUID consultaId,
            @Valid @RequestBody TratamientoCreateRequest request) {
        TratamientoResponse response = tratamientoService.prescribirTratamiento(consultaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consultas/{consultaId}/tratamientos")
    @Operation(summary = "Listar tratamientos de una consulta",
            description = "Retorna el historial de recetas y medicamentos prescritos en una consulta con sus dosis totales calculadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tratamientos obtenida"),
            @ApiResponse(responseCode = "404", description = "Consulta no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<TratamientoResponse>> listarTratamientosPorConsulta(@PathVariable UUID consultaId) {
        List<TratamientoResponse> lista = tratamientoService.listarPorConsulta(consultaId);
        return ResponseEntity.ok(lista);
    }
}
