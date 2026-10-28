package com.clinivet.arias.service;

import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
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
}
