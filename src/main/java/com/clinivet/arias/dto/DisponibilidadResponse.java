package com.clinivet.arias.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO que encapsula el resultado de la consulta de disponibilidad de un veterinario (RF-14 / RN-03 / RN-11).
 */
@Schema(description = "Resultado de la consulta de disponibilidad de un médico veterinario")
public class DisponibilidadResponse {

    @Schema(description = "ID del veterinario consultado", example = "a0000000-0000-0000-0000-000000000001")
    private UUID veterinarioId;

    @Schema(description = "Nombre completo del médico veterinario", example = "Dr. Beckhan Elias")
    private String veterinarioNombre;

    @Schema(description = "Número de colegiatura CMVP", example = "CMVP-14892")
    private String numeroColegiatura;

    @Schema(description = "Especialidad médica", example = "Medicina General y Cirugía Menor")
    private String especialidad;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Fecha consultada (yyyy-MM-dd)", example = "2026-11-02")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(description = "Hora consultada (HH:mm:ss)", example = "10:00:00")
    private LocalTime hora;

    @Schema(description = "Indica si el veterinario se encuentra disponible (true) o no (false)", example = "true")
    private boolean disponible;

    @Schema(description = "Detalle o motivo en caso de no disponibilidad o confirmación", example = "Horario disponible para agendar")
    private String mensaje;

    @Schema(description = "Franjas horarias configuradas para el día consultado", example = "[\"09:00 - 13:00\", \"14:00 - 18:00\"]")
    private List<String> turnosDelDia;

    public DisponibilidadResponse() {
    }

    public DisponibilidadResponse(UUID veterinarioId, String veterinarioNombre, String numeroColegiatura,
                                  String especialidad, LocalDate fecha, LocalTime hora,
                                  boolean disponible, String mensaje, List<String> turnosDelDia) {
        this.veterinarioId = veterinarioId;
        this.veterinarioNombre = veterinarioNombre;
        this.numeroColegiatura = numeroColegiatura;
        this.especialidad = especialidad;
        this.fecha = fecha;
        this.hora = hora;
        this.disponible = disponible;
        this.mensaje = mensaje;
        this.turnosDelDia = turnosDelDia;
    }

    // Getters y Setters
    public UUID getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(UUID veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public String getVeterinarioNombre() {
        return veterinarioNombre;
    }

    public void setVeterinarioNombre(String veterinarioNombre) {
        this.veterinarioNombre = veterinarioNombre;
    }

    public String getNumeroColegiatura() {
        return numeroColegiatura;
    }

    public void setNumeroColegiatura(String numeroColegiatura) {
        this.numeroColegiatura = numeroColegiatura;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<String> getTurnosDelDia() {
        return turnosDelDia;
    }

    public void setTurnosDelDia(List<String> turnosDelDia) {
        this.turnosDelDia = turnosDelDia;
    }
}
