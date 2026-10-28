package com.clinivet.arias.service;

import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;

import java.util.UUID;

/**
 * Contrato de servicio para la gestión de mascotas y pacientes veterinarios.
 * Cumple con el Principio de Inversión de Dependencias (DIP).
 */
public interface IMascotaService {

    /**
     * Registra una nueva mascota vinculándola obligatoriamente a un propietario existente (RF-09 / RN-01).
     * Si el propietario no existe, lanza IllegalArgumentException (HTTP 400).
     *
     * @param request datos de la mascota a registrar
     * @return respuesta detallada con los datos de la mascota y del propietario vinculado
     */
    MascotaResponse registrar(MascotaCreateRequest request);

    /**
     * Consulta la ficha completa de una mascota por su identificador UUID (RF-10).
     * Si la mascota no existe, lanza ResourceNotFoundException (404).
     * El peso mostrado es el más reciente registrado en consultas médicas o el inicial si no tiene consultas.
     *
     * @param id identificador único de la mascota
     * @return ficha completa de la mascota con datos del propietario vinculado
     */
    MascotaResponse obtenerPorId(UUID id);

    /**
     * Actualiza los datos de una mascota registrada (RF-11).
     * El vínculo con el propietario original es permanente y no puede ser modificado.
     * Si la mascota no existe, lanza ResourceNotFoundException (404).
     *
     * @param id identificador único de la mascota a actualizar
     * @param request datos modificados de la mascota
     * @return respuesta con los datos actualizados de la mascota
     */
    MascotaResponse actualizar(UUID id, MascotaUpdateRequest request);
}
