package com.clinivet.arias.service;

import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;
import com.clinivet.arias.dto.DiagnosticoRequest;
import com.clinivet.arias.dto.DiagnosticoResponse;

import java.util.UUID;

/**
 * Contrato de servicio para el registro de consultas médicas (RF-12 / RN-04 / RN-09).
 */
public interface IConsultaService {

    /**
     * Registra una consulta médica vinculada a una cita, marca la cita como atendida
     * y actualiza el peso actual de la mascota.
     *
     * @param request datos clínicos de la atención
     * @return consulta creada con su identificador
     */
    ConsultaResponse registrarConsulta(ConsultaRequest request);

    /**
     * Registra el diagnóstico clínico de una consulta médica (RF-21).
     * Solo puede haber un diagnóstico por consulta; si ya existe, lanza DiagnosticoDuplicadoException (409).
     * Si la consulta no existe, lanza ResourceNotFoundException (404).
     *
     * @param consultaId identificador de la consulta médica
     * @param request datos del diagnóstico clínico
     * @return diagnóstico registrado con sus metadatos
     */
    DiagnosticoResponse registrarDiagnostico(UUID consultaId, DiagnosticoRequest request);
}
