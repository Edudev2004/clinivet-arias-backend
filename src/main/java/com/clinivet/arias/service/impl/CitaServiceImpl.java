package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.CitaRequest;
import com.clinivet.arias.dto.CitaResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.*;
import com.clinivet.arias.repository.*;
import com.clinivet.arias.service.ICitaService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación de la lógica de negocio para la gestión de citas médicas (RF-13).
 * Aplica principios SOLID:
 * - SRP: Exclusivamente responsable de validar y registrar citas médicas.
 * - DIP: Inyección estricta por constructor de interfaces de repositorio y servicios.
 * - Defensive programming con Google Guava y Apache Commons.
 */
@Service
public class CitaServiceImpl implements ICitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaServiceImpl.class);

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaServiceImpl(CitaRepository citaRepository,
                           MascotaRepository mascotaRepository,
                           PropietarioRepository propietarioRepository,
                           VeterinarioRepository veterinarioRepository,
                           UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.propietarioRepository = propietarioRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public CitaResponse registrarCita(CitaRequest request, String emailUsuarioActual) {
        Preconditions.checkNotNull(request, "La solicitud de cita médica no puede ser nula");
        Preconditions.checkNotNull(request.getMascotaId(), "El ID de la mascota es requerido");
        Preconditions.checkNotNull(request.getPropietarioId(), "El ID del propietario es requerido");
        Preconditions.checkNotNull(request.getVeterinarioId(), "El ID del veterinario es requerido");
        Preconditions.checkNotNull(request.getFecha(), "La fecha de la cita es requerida");
        Preconditions.checkNotNull(request.getHora(), "La hora de la cita es requerida");

        LocalDateTime fechaHora = LocalDateTime.of(request.getFecha(), request.getHora());
        if (fechaHora.isBefore(LocalDateTime.now())) {
            log.warn("Rechazo de cita: fecha/hora solicitada [{}] se encuentra en el pasado", fechaHora);
            throw new IllegalArgumentException("No se puede agendar una cita en una fecha u hora pasada");
        }

        // 1. Validar existencia de la mascota
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + request.getMascotaId()));

        // 2. Validar existencia del propietario
        Propietario propietario = propietarioRepository.findById(request.getPropietarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado con ID: " + request.getPropietarioId()));

        // 3. Validar que la mascota pertenezca al propietario indicado (RN-01)
        if (mascota.getPropietario() == null || !mascota.getPropietario().getId().equals(propietario.getId())) {
            log.warn("Conflicto de pertenencia: la mascota [{}] no pertenece al propietario [{}]",
                    mascota.getId(), propietario.getId());
            throw new IllegalArgumentException("La mascota especificada no pertenece al propietario indicado");
        }

        // 4. Validar estado activo de la mascota
        if (Boolean.FALSE.equals(mascota.getActivo())) {
            log.warn("Rechazo de cita: la mascota [{}] se encuentra inactiva", mascota.getId());
            throw new IllegalArgumentException("La mascota seleccionada se encuentra inactiva en el sistema");
        }

        // 5. Validar existencia del veterinario
        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarioId()));

        // 6. Validar estado activo del veterinario
        if (Boolean.FALSE.equals(veterinario.getActivo())) {
            log.warn("Rechazo de cita: el veterinario [{}] se encuentra inactivo", veterinario.getId());
            throw new IllegalArgumentException("El médico veterinario seleccionado se encuentra inactivo");
        }

        // 7. Determinar usuario auditor (creadoPor)
        UUID creadoPorId = null;
        String creadoPorEmail = null;
        if (StringUtils.isNotBlank(emailUsuarioActual)) {
            Usuario usuarioAuditor = usuarioRepository.findByEmail(emailUsuarioActual).orElse(null);
            if (usuarioAuditor != null) {
                creadoPorId = usuarioAuditor.getId();
                creadoPorEmail = usuarioAuditor.getEmail();
            }
        }
        if (creadoPorId == null && veterinario.getUsuario() != null) {
            creadoPorId = veterinario.getUsuario().getId();
            creadoPorEmail = veterinario.getUsuario().getEmail();
        }

        // 8. Crear y persistir la entidad Cita (Estado inicial siempre PROGRAMADA según RF-13)
        Cita nuevaCita = new Cita(
                null,
                mascota.getId(),
                veterinario,
                fechaHora,
                EstadoCita.PROGRAMADA,
                request.getMotivo(),
                creadoPorId
        );

        Cita citaGuardada = citaRepository.save(nuevaCita);

        log.info("Cita médica [{}] agendada exitosamente para mascota [{}] con veterinario [{}] para el [{}]",
                citaGuardada.getId(), mascota.getNombre(), veterinario.getNombreCompleto(), fechaHora);

        return mapearACitaResponse(citaGuardada, mascota, creadoPorEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerCitaPorId(UUID id) {
        Preconditions.checkNotNull(id, "El ID de la cita no puede ser nulo");
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con ID: " + id));
        Mascota mascota = null;
        if (cita.getMascotaId() != null) {
            mascota = mascotaRepository.findById(cita.getMascotaId()).orElse(null);
        }
        String creadoPorEmail = null;
        if (cita.getCreadoPor() != null) {
            creadoPorEmail = usuarioRepository.findById(cita.getCreadoPor())
                    .map(Usuario::getEmail)
                    .orElse(null);
        }
        return mapearACitaResponse(cita, mascota, creadoPorEmail);
    }

    private CitaResponse mapearACitaResponse(Cita cita, Mascota masc, String creadoPorEmail) {
        Propietario prop = masc != null ? masc.getPropietario() : null;
        Veterinario vet = cita.getVeterinario();

        return new CitaResponse(
                cita.getId(),
                cita.getMascotaId(),
                masc != null ? masc.getNombre() : null,
                masc != null ? masc.getEspecie() : null,
                prop != null ? prop.getId() : null,
                prop != null ? prop.getNombreCompleto() : null,
                prop != null ? prop.getDni() : null,
                prop != null ? prop.getTelefono() : null,
                vet != null ? vet.getId() : null,
                vet != null ? vet.getNombreCompleto() : null,
                vet != null && vet.getEspecialidad() != null ? vet.getEspecialidad().getNombre() : null,
                cita.getFechaHora().toLocalDate(),
                cita.getFechaHora().toLocalTime(),
                cita.getFechaHora(),
                cita.getEstado() != null ? cita.getEstado().getDisplayValue() : "PROGRAMADA",
                cita.getMotivo(),
                cita.getCreadoPor(),
                creadoPorEmail,
                cita.getCreatedAt()
        );
    }
}
