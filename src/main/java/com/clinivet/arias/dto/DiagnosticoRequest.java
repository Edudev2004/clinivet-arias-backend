package com.clinivet.arias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de registro de diagnóstico clínico por consulta médica (RF-21).
 */
@Schema(description = "Solicitud para registrar el diagnóstico definitivo de una atención clínica")
public class DiagnosticoRequest {

    @NotBlank(message = "La descripción del diagnóstico es obligatoria")
    @Size(max = 1000, message = "La descripción del diagnóstico no debe superar los 1000 caracteres")
    @Schema(description = "Conclusión clínica o diagnóstico de la atención", example = "Gastroenteritis aguda por ingesta de cuerpo extraño")
    private String descripcion;

    @Size(max = 20, message = "El código CIE no debe superar los 20 caracteres")
    @Schema(description = "Código internacional de enfermedades veterinarias (opcional)", example = "K52.9", nullable = true)
    private String codigoCIE;

    public DiagnosticoRequest() {
    }

    public DiagnosticoRequest(String descripcion, String codigoCIE) {
        this.descripcion = descripcion;
        this.codigoCIE = codigoCIE;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoCIE() {
        return codigoCIE;
    }

    public void setCodigoCIE(String codigoCIE) {
        this.codigoCIE = codigoCIE;
    }
}
