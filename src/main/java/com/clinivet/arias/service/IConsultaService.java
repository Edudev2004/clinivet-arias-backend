package com.clinivet.arias.service;

import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;

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
}
