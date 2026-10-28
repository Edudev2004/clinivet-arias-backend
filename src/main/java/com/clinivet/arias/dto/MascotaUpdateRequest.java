package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para la solicitud de actualización de datos de una mascota (RF-11).
 * Nota: El propietario no puede ser modificado ya que el vínculo es permanente.
 */
@Schema(description = "Payload para modificar los datos de una mascota registrada")
public class MascotaUpdateRequest {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe contener entre 2 y 100 caracteres")
    @Schema(description = "Nombre de la mascota", example = "Firulais")
    private String nombre;

    @NotBlank(message = "La especie animal es obligatoria")
    @Size(min = 2, max = 50, message = "La especie debe contener entre 2 y 50 caracteres")
    @Schema(description = "Especie animal (Canino, Felino, etc.)", example = "Canino")
    private String especie;

    @Size(max = 100, message = "La raza no puede superar los 100 caracteres")
    @Schema(description = "Raza de la mascota", example = "Golden Retriever")
    private String raza;

    @Size(max = 50, message = "El color no puede superar los 50 caracteres")
    @Schema(description = "Color o pelaje", example = "Dorado claro")
    private String color;

    @NotBlank(message = "El sexo de la mascota es obligatorio")
    @Pattern(regexp = "^(?i)(Macho|Hembra|M|H)$", message = "El sexo debe ser 'Macho', 'Hembra', 'M' o 'H'")
    @Schema(description = "Sexo del paciente", example = "Macho")
    private String sexo;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de nacimiento aproximada o exacta", example = "2022-03-10")
    private LocalDate fechaNacimiento;

    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero")
    @Digits(integer = 3, fraction = 2, message = "El peso no puede superar 3 enteros y 2 decimales")
    @JsonAlias({"peso", "pesoActualKg"})
    @Schema(description = "Peso corporal en kilogramos", example = "29.80")
    private BigDecimal pesoActualKg;

    @Schema(description = "URL opcional de la fotografía de la mascota", example = "https://clinivet.com/photos/firulais.jpg")
    private String fotoUrl;

    @Schema(description = "Estado de actividad de la mascota", example = "true")
    private Boolean activo;

    public MascotaUpdateRequest() {
    }

    public MascotaUpdateRequest(String nombre, String especie, String raza, String color, String sexo,
                                LocalDate fechaNacimiento, BigDecimal pesoActualKg, String fotoUrl, Boolean activo) {
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.color = color;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.pesoActualKg = pesoActualKg;
        this.fotoUrl = fotoUrl;
        this.activo = activo != null ? activo : true;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public BigDecimal getPesoActualKg() {
        return pesoActualKg;
    }

    public void setPesoActualKg(BigDecimal pesoActualKg) {
        this.pesoActualKg = pesoActualKg;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
