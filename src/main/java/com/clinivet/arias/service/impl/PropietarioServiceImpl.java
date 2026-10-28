package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.exception.DuplicateResourceException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.IPropietarioService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación de la lógica de negocio para la gestión de propietarios (RF-05).
 * Aplica principios SOLID:
 * - SRP: Exclusivamente enfocado en reglas de negocio de propietarios.
 * - DIP: Implementa la interfaz IPropietarioService y depende de la abstracción PropietarioRepository.
 * Integra librerías de apoyo: Google Guava (Preconditions), Apache Commons (StringUtils) y Logback (SLF4J).
 */
@Service
public class PropietarioServiceImpl implements IPropietarioService {

    private static final Logger log = LoggerFactory.getLogger(PropietarioServiceImpl.class);

    private final PropietarioRepository propietarioRepository;

    public PropietarioServiceImpl(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }

    @Override
    @Transactional
    public PropietarioResponse registrar(PropietarioCreateRequest request) {
        // 1. Validación defensiva con Google Guava
        Preconditions.checkNotNull(request, "La solicitud de registro no puede ser nula");
        Preconditions.checkNotNull(request.getDni(), "El DNI del propietario no puede ser nulo");
        Preconditions.checkNotNull(request.getNombre(), "El nombre del propietario no puede ser nulo");
        Preconditions.checkNotNull(request.getApellido(), "El apellido del propietario no puede ser nulo");

        // 2. Limpieza y normalización de textos con Apache Commons
        String dni = StringUtils.trim(request.getDni());
        String nombre = StringUtils.trim(request.getNombre());
        String apellido = StringUtils.trim(request.getApellido());
        String telefono = StringUtils.trim(request.getTelefono());
        String email = StringUtils.trimToNull(request.getEmail());
        String direccion = StringUtils.trimToNull(request.getDireccion());

        log.info("Iniciando registro de nuevo propietario con DNI [{}] y nombre [{} {}]", dni, nombre, apellido);

        // 3. Regla de negocio: El DNI debe ser único en el sistema
        if (propietarioRepository.findByDni(dni).isPresent()) {
            log.warn("Rechazo de registro: ya existe un propietario registrado con el DNI [{}]", dni);
            throw new DuplicateResourceException("Ya existe un propietario registrado con el DNI: " + dni);
        }

        // 4. Creación y persistencia de la entidad
        Propietario nuevoPropietario = new Propietario(
                null,
                nombre,
                apellido,
                dni,
                telefono,
                email,
                direccion,
                true
        );

        Propietario guardado = propietarioRepository.save(nuevoPropietario);

        log.info("Propietario [{} {}] registrado exitosamente con ID [{}] (RF-05)",
                guardado.getNombre(), guardado.getApellido(), guardado.getId());

        return mapearAResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PropietarioResponse obtenerPorId(UUID id) {
        Preconditions.checkNotNull(id, "El identificador del propietario no puede ser nulo");

        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + id));

        return mapearAResponse(propietario);
    }

    private PropietarioResponse mapearAResponse(Propietario p) {
        return new PropietarioResponse(
                p.getId(),
                p.getNombre(),
                p.getApellido(),
                p.getDni(),
                p.getTelefono(),
                p.getEmail(),
                p.getDireccion(),
                p.getActivo(),
                p.getCreatedAt()
        );
    }
}
