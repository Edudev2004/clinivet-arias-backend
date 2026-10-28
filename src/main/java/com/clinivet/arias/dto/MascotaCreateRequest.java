package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para la solicitud de registro de una nueva mascota vinculada a un propietario (RF-09).
 */
@Schema(description = "Payload para registrar un nuevo paciente veterinario vinculado a un propietario")
public class MascotaCreateRequest {

    @NotNull(message = "El identificador del propietario es obligatorio (RN-01)")
    @Schema(description = "Identificador UUID del propietario vinculado", example = "e0000000-0000-0000-0000-000000000001")
    private UUID propietarioId;

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
    @Schema(description = "Color o características del pelaje", example = "Dorado")
    private String color;

    @NotBlank(message = "El sexo de la mascota es obligatorio")
    @Pattern(regexp = "^(?i)(Macho|Hembra|M|H)$", message = "El sexo debe ser 'Macho', 'Hembra', 'M' o 'H'")
    @Schema(description = "Sexo del paciente", example = "Macho")
    private String sexo;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de nacimiento aproximada o exacta", example = "2023-05-15")
    private LocalDate fechaNacimiento;

    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero")
    @Digits(integer = 3, fraction = 2, message = "El peso no puede superar 3 enteros y 2 decimales")
    @JsonAlias({"peso", "pesoActualKg"})
    @Schema(description = "Peso corporal en kilogramos", example = "28.50")
    private BigDecimal pesoActualKg;

    @Schema(description = "URL opcional de la foto de la mascota", example = "https://clinivet.com/photos/firulais.jpg")
    private String fotoUrl;

    public MascotaCreateRequest() {
    }

    public MascotaCreateRequest(UUID propietarioId, String nombre, String especie, String raza,
                                String color, String sexo, LocalDate fechaNacimiento,
                                BigDecimal pesoActualKg, String fotoUrl) {
        this.propietarioId = propietarioId;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.color = color;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.pesoActualKg = pesoActualKg;
        this.fotoUrl = fotoUrl;
    }

    public UUID getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(UUID propietarioId) {
        this.propietarioId = propietarioId;
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
}
