package com.clinivet.arias.service;

import com.clinivet.arias.dto.DisponibilidadResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Contrato de servicio para operaciones sobre médicos veterinarios y control de agenda.
 * Cumple con SRP (lógica de agenda y disponibilidad de especialistas), DIP, RN-03, RN-11 y RF-14.
 */
public interface IVeterinarioService {

    /**
     * Consulta la disponibilidad de un veterinario en una fecha y hora específicas (RF-14).
     * Valida franjas horarias configuradas (RN-11) y previene cruces de citas (RN-03 / RF-15).
     *
     * @param veterinarioId identificador UUID del veterinario
     * @param fecha fecha deseada de la atención
     * @param hora hora deseada de la atención
     * @return respuesta detallada con indicador disponible true/false y turnos del día
     */
    DisponibilidadResponse consultarDisponibilidad(UUID veterinarioId, LocalDate fecha, LocalTime hora);

    /**
     * Valida internamente si un veterinario está disponible en la fecha y hora indicadas.
     * Método reutilizable por servicios de Citas para creación y reprogramación (RF-13 / RF-15 / RF-16).
     *
     * @param veterinarioId identificador UUID del veterinario
     * @param fechaHora fecha y hora de la cita
     * @return true si tiene turno configurado y no tiene citas programadas a esa hora; false en caso contrario
     */
    boolean esHorarioDisponible(UUID veterinarioId, LocalDateTime fechaHora);
}
