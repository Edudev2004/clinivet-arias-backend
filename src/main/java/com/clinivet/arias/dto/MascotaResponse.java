package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

/**
 * DTO de respuesta para la información de una mascota (RF-08, RF-09, RF-10).
 */
@Schema(description = "Respuesta detallada con información de la mascota y su propietario")
public class MascotaResponse {

    @Schema(description = "Identificador único de la mascota (UUID)", example = "f0000000-0000-0000-0000-000000000001")
    private UUID id;

    @Schema(description = "Nombre de la mascota", example = "Firulais")
    private String nombre;

    @Schema(description = "Especie animal (Canino, Felino, etc.)", example = "Canino")
    private String especie;

    @Schema(description = "Raza de la mascota", example = "Golden Retriever")
    private String raza;

    @Schema(description = "Color o pelaje de la mascota", example = "Dorado")
    private String color;

    @Schema(description = "Sexo de la mascota (Macho / Hembra)", example = "Macho")
    private String sexo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha de nacimiento estimada o exacta", example = "2023-05-15")
    private LocalDate fechaNacimiento;

    @Schema(description = "Edad calculada legible", example = "1 año, 5 meses")
    private String edad;

    @Schema(description = "Peso actual registrado en kg", example = "28.50")
    private BigDecimal pesoActualKg;

    @Schema(description = "URL pública de la fotografía de la mascota", example = "https://clinivet.com/photos/firulais.jpg")
    private String fotoUrl;

    @Schema(description = "Estado de actividad de la mascota", example = "true")
    private Boolean activo;

    @Schema(description = "Identificador del propietario vinculado", example = "e0000000-0000-0000-0000-000000000001")
    private UUID propietarioId;

    @Schema(description = "Nombre completo del propietario vinculado", example = "Carlos Mendoza Ramos")
    private String propietarioNombre;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha de registro en el sistema", example = "2026-10-27T10:00:00")
    private LocalDateTime createdAt;

    public MascotaResponse() {
    }

    public MascotaResponse(UUID id, String nombre, String especie, String raza, String color, String sexo,
                           LocalDate fechaNacimiento, BigDecimal pesoActualKg, String fotoUrl,
                           Boolean activo, UUID propietarioId, String propietarioNombre, LocalDateTime createdAt) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.color = color;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = calcularEdadLegible(fechaNacimiento);
        this.pesoActualKg = pesoActualKg;
        this.fotoUrl = fotoUrl;
        this.activo = activo != null ? activo : true;
        this.propietarioId = propietarioId;
        this.propietarioNombre = propietarioNombre;
        this.createdAt = createdAt;
    }

    private static String calcularEdadLegible(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            return "Desconocida";
        }
        LocalDate hoy = LocalDate.now();
        if (fechaNacimiento.isAfter(hoy)) {
            return "0 meses";
        }
        Period periodo = Period.between(fechaNacimiento, hoy);
        int anios = periodo.getYears();
        int meses = periodo.getMonths();

        if (anios > 0 && meses > 0) {
            return anios + (anios == 1 ? " año, " : " años, ") + meses + (meses == 1 ? " mes" : " meses");
        } else if (anios > 0) {
            return anios + (anios == 1 ? " año" : " años");
        } else if (meses > 0) {
            return meses + (meses == 1 ? " mes" : " meses");
        } else {
            int dias = periodo.getDays();
            return dias + (dias == 1 ? " día" : " días");
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
        this.edad = calcularEdadLegible(fechaNacimiento);
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
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

    public UUID getPropietarioId() {
        return propietarioId;
    }

    public void setPropietarioId(UUID propietarioId) {
        this.propietarioId = propietarioId;
    }

    public String getPropietarioNombre() {
        return propietarioNombre;
    }

    public void setPropietarioNombre(String propietarioNombre) {
        this.propietarioNombre = propietarioNombre;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
