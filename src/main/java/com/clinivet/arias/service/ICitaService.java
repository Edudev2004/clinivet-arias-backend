package com.clinivet.arias.service;

import com.clinivet.arias.dto.CitaRequest;
import com.clinivet.arias.dto.CitaResponse;

import java.util.UUID;

/**
 * Contrato de servicio para la gestión de citas médicas (Épica 3 de APF2).
 * Cumple con el Principio de Inversión de Dependencias (DIP) y SRP.
 */
public interface ICitaService {

    /**
     * Registra una nueva cita médica validando existencia de entidades, pertenencia y estado (RF-13).
     *
     * @param request datos de la cita médica
     * @param emailUsuarioActual email del usuario autenticado que realiza el agendamiento
     * @return detalle completo de la cita médica creada
     */
    CitaResponse registrarCita(CitaRequest request, String emailUsuarioActual);

    /**
     * Consulta el detalle de una cita médica por su identificador único.
     *
     * @param id identificador UUID de la cita
     * @return detalle de la cita médica
     */
    CitaResponse obtenerCitaPorId(UUID id);

    /**
     * Reprograma una cita médica existente a una nueva fecha y hora (RF-16).
     *
     * @param id identificador UUID de la cita
     * @param request datos con la nueva fecha y hora
     * @return detalle actualizado de la cita médica
     */
    CitaResponse reprogramarCita(UUID id, com.clinivet.arias.dto.CitaReprogramarRequest request);

    /**
     * Cancela una cita médica conservando el registro histórico en base de datos (RF-17 / RN-07).
     *
     * @param id identificador UUID de la cita
     * @return detalle actualizado de la cita médica con estado CANCELADA
     */
    CitaResponse cancelarCita(UUID id);

    /**
     * Consulta una lista paginada de citas médicas aplicando filtros opcionales por veterinario, fechas y estado (RF-18).
     * Siempre retorna los resultados ordenados por fecha y hora ascendente.
     *
     * @param veterinarioId identificador opcional del veterinario
     * @param fecha fecha exacta opcional
     * @param fechaInicio fecha inicial opcional para rango
     * @param fechaFin fecha final opcional para rango
     * @param estado estado opcional de la cita
     * @param pageable configuración de paginación
     * @return página con citas médicas encontradas
     */
    org.springframework.data.domain.Page<CitaResponse> listarCitasConFiltros(
            UUID veterinarioId,
            java.time.LocalDate fecha,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin,
            String estado,
            org.springframework.data.domain.Pageable pageable
    );

    /**
     * Actualiza el estado de una cita médica validando las transiciones de ciclo de vida permitidas (RF-19).
     * Solo permite transiciones: PROGRAMADA → ATENDIDA, PROGRAMADA → CANCELADA.
     *
     * @param id identificador UUID de la cita
     * @param request solicitud con el nuevo estado
     * @return detalle actualizado de la cita médica
     */
    CitaResponse actualizarEstadoCita(UUID id, com.clinivet.arias.dto.CitaEstadoRequest request);
}
