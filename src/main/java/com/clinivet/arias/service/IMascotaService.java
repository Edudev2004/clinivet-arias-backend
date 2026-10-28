package com.clinivet.arias.service;

import com.clinivet.arias.dto.HistorialConsultaResponse;
import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
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

    /**
     * Lista todas las mascotas activas en el sistema con su peso más reciente actualizado en consulta médica (RF-10 / RF-12).
     *
     * @return lista de fichas de mascotas activas con su peso vigente
     */
    List<MascotaResponse> listarTodas();

    /**
     * Consulta el historial clínico completo de una mascota ordenado cronológicamente con filtros opcionales (RF-25 / RF-26).
     *
     * @param mascotaId identificador único de la mascota
     * @param fechaInicio fecha inicial del filtro opcional
     * @param fechaFin fecha final del filtro opcional
     * @param veterinarioId identificador del veterinario del filtro opcional
     * @param pageable configuración de paginación (por defecto 20 registros)
     * @return página con el historial de consultas médicas, diagnósticos, tratamientos y vacunas
     */
    Page<HistorialConsultaResponse> obtenerHistorial(
            UUID mascotaId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            UUID veterinarioId,
            Pageable pageable
    );
}
