package com.clinivet.arias.service;

import com.clinivet.arias.dto.VacunaAplicadaRequest;
import com.clinivet.arias.dto.VacunaAplicadaResponse;
import com.clinivet.arias.model.VacunaCatalogo;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la gestión y registro de vacunas aplicadas a mascotas (RF-24).
 * Cumple con ISP y DIP (SOLID).
 */
public interface IVacunaService {

    /**
     * Registra una vacuna aplicada a una mascota específica.
     *
     * @param mascotaId identificador de la mascota receptora
     * @param request datos de la aplicación de la vacuna
     * @return detalle de la vacuna registrada
     */
    VacunaAplicadaResponse registrarVacuna(UUID mascotaId, VacunaAplicadaRequest request);

    /**
     * Lista el historial completo de vacunas aplicadas a una mascota.
     *
     * @param mascotaId identificador de la mascota
     * @return lista de vacunas aplicadas ordenadas cronológicamente descendente
     */
    List<VacunaAplicadaResponse> listarPorMascota(UUID mascotaId);

    /**
     * Lista todas las vacunas disponibles en el catálogo maestro.
     *
     * @return catálogo de vacunas
     */
    List<VacunaCatalogo> listarCatalogo();
}
