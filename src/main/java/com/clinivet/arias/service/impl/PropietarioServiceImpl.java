package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.PropietarioCreateRequest;
import com.clinivet.arias.dto.PropietarioResponse;
import com.clinivet.arias.dto.PropietarioUpdateRequest;
import com.clinivet.arias.exception.DuplicateResourceException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.IPropietarioService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para la gestión de propietarios (RF-05, RF-06).
 * Aplica principios SOLID:
 * - SRP: Exclusivamente enfocado en reglas de negocio de propietarios.
 * - DIP: Implementa la interfaz IPropietarioService y depende de abstracciones Spring Data JPA.
 * Integra librerías de apoyo: Google Guava (Preconditions), Apache Commons (StringUtils) y Logback (SLF4J).
 */
@Service
public class PropietarioServiceImpl implements IPropietarioService {

    private static final Logger log = LoggerFactory.getLogger(PropietarioServiceImpl.class);

    private final PropietarioRepository propietarioRepository;
    private final MascotaRepository mascotaRepository;

    public PropietarioServiceImpl(PropietarioRepository propietarioRepository, MascotaRepository mascotaRepository) {
        this.propietarioRepository = propietarioRepository;
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    @Transactional
    public PropietarioResponse registrar(PropietarioCreateRequest request) {
        Preconditions.checkNotNull(request, "La solicitud de registro no puede ser nula");
        Preconditions.checkNotNull(request.getDni(), "El DNI del propietario no puede ser nulo");
        Preconditions.checkNotNull(request.getNombre(), "El nombre del propietario no puede ser nulo");
        Preconditions.checkNotNull(request.getApellido(), "El apellido del propietario no puede ser nulo");

        String dni = StringUtils.trim(request.getDni());
        String nombre = StringUtils.trim(request.getNombre());
        String apellido = StringUtils.trim(request.getApellido());
        String telefono = StringUtils.trim(request.getTelefono());
        String email = StringUtils.trimToNull(request.getEmail());
        String direccion = StringUtils.trimToNull(request.getDireccion());

        log.info("Iniciando registro de nuevo propietario con DNI [{}] y nombre [{} {}]", dni, nombre, apellido);

        if (propietarioRepository.findByDni(dni).isPresent()) {
            log.warn("Rechazo de registro: ya existe un propietario registrado con el DNI [{}]", dni);
            throw new DuplicateResourceException("Ya existe un propietario registrado con el DNI: " + dni);
        }

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

    @Override
    @Transactional(readOnly = true)
    public Page<PropietarioResponse> buscar(String termino, Pageable pageable) {
        Pageable paginacion = pageable != null ? pageable : PageRequest.of(0, 20);
        String query = StringUtils.trimToNull(termino);

        log.debug("Ejecutando búsqueda de propietarios con término [{}] y paginación [{}] (RF-06)", query, paginacion);

        if (query == null) {
            Page<Propietario> pagina = propietarioRepository.findByActivoTrue(paginacion);
            return pagina.map(this::mapearAResponse);
        }

        // Si el término corresponde a un formato UUID, buscar directamente por ID
        try {
            UUID idBuscado = UUID.fromString(query);
            Optional<Propietario> porId = propietarioRepository.findById(idBuscado);
            if (porId.isPresent()) {
                List<PropietarioResponse> lista = Collections.singletonList(mapearAResponse(porId.get()));
                return new PageImpl<>(lista, paginacion, 1);
            }
        } catch (IllegalArgumentException ignored) {
            // No es UUID, continuar búsqueda textual
        }

        Page<Propietario> resultados = propietarioRepository.buscarPorTermino(query, paginacion);
        log.debug("Búsqueda completada: {} propietarios encontrados", resultados.getTotalElements());

        return resultados.map(this::mapearAResponse);
    }

    @Override
    @Transactional
    public PropietarioResponse actualizar(UUID id, com.clinivet.arias.dto.PropietarioUpdateRequest request) {
        Preconditions.checkNotNull(id, "El identificador del propietario no puede ser nulo");
        Preconditions.checkNotNull(request, "La solicitud de actualización no puede ser nula");
        Preconditions.checkNotNull(request.getDni(), "El DNI del propietario no puede ser nulo");
        Preconditions.checkNotNull(request.getNombre(), "El nombre del propietario no puede ser nulo");
        Preconditions.checkNotNull(request.getApellido(), "El apellido del propietario no puede ser nulo");

        String dni = StringUtils.trim(request.getDni());
        String nombre = StringUtils.trim(request.getNombre());
        String apellido = StringUtils.trim(request.getApellido());
        String telefono = StringUtils.trim(request.getTelefono());
        String email = StringUtils.trimToNull(request.getEmail());
        String direccion = StringUtils.trimToNull(request.getDireccion());

        log.info("Iniciando actualización de datos para propietario [{}] con DNI [{}] (RF-07)", id, dni);

        // 1. Verificar existencia del propietario (404 si no existe)
        Propietario propietario = propietarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + id));

        // 2. Si el DNI cambió, validar que no esté en uso por otro propietario (409 si duplicado)
        if (!dni.equalsIgnoreCase(propietario.getDni())) {
            Optional<Propietario> existenteConDni = propietarioRepository.findByDni(dni);
            if (existenteConDni.isPresent() && !existenteConDni.get().getId().equals(id)) {
                log.warn("Rechazo de actualización: el DNI [{}] ya se encuentra registrado por otro propietario [{}]",
                        dni, existenteConDni.get().getId());
                throw new DuplicateResourceException("El DNI " + dni + " ya se encuentra registrado por otro propietario");
            }
        }

        // 3. Actualizar campos
        propietario.setNombre(nombre);
        propietario.setApellido(apellido);
        propietario.setDni(dni);
        propietario.setTelefono(telefono);
        propietario.setEmail(email);
        propietario.setDireccion(direccion);
        if (request.getActivo() != null) {
            propietario.setActivo(request.getActivo());
        }

        Propietario actualizado = propietarioRepository.save(propietario);
        log.info("Propietario [{} {}] actualizado exitosamente con ID [{}] (RF-07)",
                actualizado.getNombre(), actualizado.getApellido(), id);

        return mapearAResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MascotaResponse> listarMascotasPorPropietario(UUID propietarioId) {
        Preconditions.checkNotNull(propietarioId, "El identificador del propietario no puede ser nulo");

        log.debug("Consultando mascotas del propietario [{}] (RF-08)", propietarioId);

        // 1. Validar que el propietario exista (404 si no existe)
        Propietario propietario = propietarioRepository.findById(propietarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + propietarioId));

        // 2. Obtener lista de mascotas activas vinculadas
        List<Mascota> mascotas = mascotaRepository.findByPropietarioIdAndActivoTrue(propietarioId);

        log.info("Se encontraron [{}] mascotas para el propietario [{}] (RF-08)", mascotas.size(), propietarioId);

        String nombreCompleto = propietario.getNombre() + " " + propietario.getApellido();

        return mascotas.stream()
                .map(m -> mapearMascotaAResponse(m, nombreCompleto))
                .toList();
    }

    private MascotaResponse mapearMascotaAResponse(Mascota m, String propietarioNombre) {
        UUID propId = m.getPropietario() != null ? m.getPropietario().getId() : null;
        String propNom = propietarioNombre;
        if (m.getPropietario() != null && m.getPropietario().getNombre() != null) {
            propNom = m.getPropietario().getNombre() + " " + m.getPropietario().getApellido();
        }

        return new MascotaResponse(
                m.getId(),
                m.getNombre(),
                m.getEspecie(),
                m.getRaza(),
                m.getColor(),
                m.getSexo(),
                m.getFechaNacimiento(),
                m.getPesoActualKg(),
                m.getFotoUrl(),
                m.getActivo(),
                propId,
                propNom,
                m.getCreatedAt()
        );
    }

    private PropietarioResponse mapearAResponse(Propietario p) {
        long cantidadMascotas = 0L;
        if (p.getId() != null && mascotaRepository != null) {
            cantidadMascotas = mascotaRepository.countByPropietarioIdAndActivoTrue(p.getId());
        }

        return new PropietarioResponse(
                p.getId(),
                p.getNombre(),
                p.getApellido(),
                p.getDni(),
                p.getTelefono(),
                p.getEmail(),
                p.getDireccion(),
                p.getActivo(),
                p.getCreatedAt(),
                cantidadMascotas
        );
    }
}
