package com.clinivet.arias.service;

import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.dto.PropietarioUpdateRequest;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de servicio para la gestión de propietarios (clientes) de la veterinaria.
 * Cumple con el Principio de Inversión de Dependencias (DIP).
 */
public interface IPropietarioService {

    /**
     * Registra un nuevo propietario en el sistema validando unicidad de DNI (RF-05).
     *
     * @param request datos del propietario a registrar
     * @return datos del propietario persistido con su ID generado
     */
    PropietarioResponse registrar(PropietarioCreateRequest request);

    /**
     * Obtiene un propietario por su identificador UUID.
     *
     * @param id identificador del propietario
     * @return datos del propietario
     */
    PropietarioResponse obtenerPorId(UUID id);

    /**
     * Busca propietarios por término (nombre, apellido, DNI, teléfono o UUID) de forma paginada (RF-06).
     * Si el término es nulo o vacío, lista todos los propietarios activos.
     * Incluye la cantidad de mascotas registradas por propietario.
     *
     * @param termino criterio de búsqueda
     * @param pageable configuración de paginación y ordenamiento
     * @return página con resultados encontrados (lista vacía si no hay coincidencias)
     */
    org.springframework.data.domain.Page<PropietarioResponse> buscar(String termino, org.springframework.data.domain.Pageable pageable);

    /**
     * Actualiza los datos de un propietario existente validando que el DNI no pertenezca a otro propietario (RF-07).
     *
     * @param id identificador del propietario
     * @param request datos a actualizar
     * @return datos actualizados del propietario
     */
    PropietarioResponse actualizar(UUID id, PropietarioUpdateRequest request);

    /**
     * Consulta todas las mascotas asociadas a un propietario (RF-08).
     * Si el propietario no existe, lanza ResourceNotFoundException (404).
     * Si el propietario no tiene mascotas, retorna una lista vacía (200).
     *
     * @param propietarioId identificador único del propietario
     * @return lista de mascotas vinculadas
     */
    List<MascotaResponse> listarMascotasPorPropietario(UUID propietarioId);
}
