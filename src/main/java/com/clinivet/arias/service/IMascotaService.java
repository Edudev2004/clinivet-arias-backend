package com.clinivet.arias.service;

import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;

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
}
