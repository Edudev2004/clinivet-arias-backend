package com.clinivet.arias.service;

import com.clinivet.arias.dto.TratamientoCreateRequest;
import com.clinivet.arias.dto.TratamientoResponse;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la gestión de prescripciones médicas y tratamientos.
 * Cumple con SRP, DIP, RN-04, RN-12 y RF-23.
 */
public interface ITratamientoService {

    /**
     * Prescribe un tratamiento en una consulta clínica calculando automáticamente la dosis total (RF-23).
     *
     * @param consultaId identificador UUID de la consulta médica
     * @param request datos del medicamento y dosis prescrita
     * @return tratamiento registrado con su dosis total calculada
     */
    TratamientoResponse prescribirTratamiento(UUID consultaId, TratamientoCreateRequest request);

    /**
     * Retorna la lista de tratamientos prescritos en una consulta médica específica.
     *
     * @param consultaId identificador UUID de la consulta
     * @return lista de tratamientos prescritos ordenados cronológicamente
     */
    List<TratamientoResponse> listarPorConsulta(UUID consultaId);
}
