package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.VacunaAplicadaRequest;
import com.clinivet.arias.dto.VacunaAplicadaResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.*;
import com.clinivet.arias.repository.*;
import com.clinivet.arias.service.IVacunaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de vacunas (RF-24).
 * Cumple con SRP (100% lógica de negocio) y DIP (inyección por constructor).
 */
@Service
public class VacunaServiceImpl implements IVacunaService {

    private static final Logger log = LoggerFactory.getLogger(VacunaServiceImpl.class);

    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final VacunaCatalogoRepository vacunaCatalogoRepository;
    private final VacunaAplicadaRepository vacunaAplicadaRepository;
    private final ConsultaRepository consultaRepository;

    public VacunaServiceImpl(MascotaRepository mascotaRepository,
                             VeterinarioRepository veterinarioRepository,
                             VacunaCatalogoRepository vacunaCatalogoRepository,
                             VacunaAplicadaRepository vacunaAplicadaRepository,
                             ConsultaRepository consultaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.vacunaCatalogoRepository = vacunaCatalogoRepository;
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
        this.consultaRepository = consultaRepository;
    }

    @Override
    @Transactional
    public VacunaAplicadaResponse registrarVacuna(UUID mascotaId, VacunaAplicadaRequest request) {
        log.info("Iniciando registro de vacuna [{}] para mascota [{}] (RF-24)", request.getNombreVacuna(), mascotaId);

        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + mascotaId));

        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarioId()));

        if (!Boolean.TRUE.equals(veterinario.getActivo())) {
            throw new IllegalArgumentException("El veterinario seleccionado no se encuentra activo");
        }

        // Obtener o registrar la vacuna en el catálogo maestro
        String nombreNormalizado = request.getNombreVacuna().trim();
        VacunaCatalogo vacuna = vacunaCatalogoRepository.findByNombreIgnoreCase(nombreNormalizado)
                .orElseGet(() -> vacunaCatalogoRepository.save(new VacunaCatalogo(nombreNormalizado)));

        Consulta consulta = null;
        if (request.getConsultaId() != null) {
            consulta = consultaRepository.findById(request.getConsultaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta médica no encontrada con ID: " + request.getConsultaId()));
        }

        VacunaAplicada vacunaAplicada = new VacunaAplicada(
                null,
                mascota,
                vacuna,
                consulta,
                veterinario,
                request.getFechaAplicacion(),
                request.getProximaFecha()
        );
        vacunaAplicada.setLote(request.getLote());

        VacunaAplicada guardada = vacunaAplicadaRepository.save(vacunaAplicada);
        log.info("Vacuna [{}] registrada exitosamente con ID [{}] para la mascota [{}]",
                vacuna.getNombre(), guardada.getId(), mascota.getNombre());

        return mapToResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacunaAplicadaResponse> listarPorMascota(UUID mascotaId) {
        log.debug("Consultando historial de vacunación de la mascota [{}] (RF-24)", mascotaId);

        if (!mascotaRepository.existsById(mascotaId)) {
            throw new ResourceNotFoundException("Mascota no encontrada con ID: " + mascotaId);
        }

        return vacunaAplicadaRepository.findByMascotaIdOrderByFechaAplicacionDesc(mascotaId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VacunaCatalogo> listarCatalogo() {
        return vacunaCatalogoRepository.findAll();
    }

    private VacunaAplicadaResponse mapToResponse(VacunaAplicada entity) {
        return new VacunaAplicadaResponse(
                entity.getId(),
                entity.getMascota().getId(),
                entity.getMascota().getNombre(),
                entity.getVacuna().getId(),
                entity.getVacuna().getNombre(),
                entity.getFechaAplicacion(),
                entity.getProximaDosis(),
                entity.getVeterinario().getId(),
                entity.getVeterinario().getNombreCompleto().trim(),
                entity.getLote(),
                entity.getConsulta() != null ? entity.getConsulta().getId() : null,
                entity.getCreatedAt()
        );
    }
}
