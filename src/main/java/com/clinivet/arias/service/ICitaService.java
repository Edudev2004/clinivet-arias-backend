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
}
