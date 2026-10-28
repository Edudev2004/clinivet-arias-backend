package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.ConsultaRequest;
import com.clinivet.arias.dto.ConsultaResponse;
import com.clinivet.arias.exception.ConsultaDuplicadaException;
import com.clinivet.arias.dto.DiagnosticoRequest;
import com.clinivet.arias.dto.DiagnosticoResponse;
import com.clinivet.arias.exception.DiagnosticoDuplicadoException;
import com.clinivet.arias.exception.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import com.clinivet.arias.model.Cita;
import com.clinivet.arias.model.Consulta;
import com.clinivet.arias.model.EstadoCita;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Veterinario;
import com.clinivet.arias.repository.CitaRepository;
import com.clinivet.arias.repository.ConsultaRepository;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.VeterinarioRepository;
import com.clinivet.arias.service.IConsultaService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del registro de consultas médicas (RF-12).
 * - SRP: valida y registra la consulta, marca la cita como atendida y actualiza el peso de la mascota.
 * - DIP: depende solo de interfaces de repositorio, inyectadas por constructor.
 */
@Service
public class ConsultaServiceImpl implements IConsultaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaServiceImpl.class);

    private final ConsultaRepository consultaRepository;
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;

    public ConsultaServiceImpl(ConsultaRepository consultaRepository,
                               CitaRepository citaRepository,
                               MascotaRepository mascotaRepository,
                               VeterinarioRepository veterinarioRepository) {
        this.consultaRepository = consultaRepository;
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @Override
    @Transactional
    public ConsultaResponse registrarConsulta(ConsultaRequest request) {
        Preconditions.checkNotNull(request, "La solicitud de consulta no puede ser nula");
        Preconditions.checkNotNull(request.getCitaId(), "El ID de la cita es requerido");
        Preconditions.checkNotNull(request.getVeterinarioId(), "El ID del veterinario es requerido");
        Preconditions.checkNotNull(request.getFecha(), "La fecha de la consulta es requerida");
        Preconditions.checkNotNull(request.getPeso(), "El peso es requerido");
        Preconditions.checkNotNull(request.getTemperatura(), "La temperatura es requerida");

        // 1. La cita debe existir
        Cita cita = citaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con ID: " + request.getCitaId()));

        // 2. El veterinario debe existir, estar activo y ser el asignado a la cita (RN-02 / RN-04)
        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarioId()));
        if (Boolean.FALSE.equals(veterinario.getActivo())) {
            throw new IllegalArgumentException("El médico veterinario seleccionado se encuentra inactivo");
        }
        if (cita.getVeterinario() == null || !cita.getVeterinario().getId().equals(veterinario.getId())) {
            log.warn("Rechazo de consulta: la cita [{}] no está asignada al veterinario [{}]", cita.getId(), veterinario.getId());
            throw new IllegalArgumentException("La cita no está asignada al veterinario indicado");
        }

        // 3. Estado de la cita: no cancelada y sin consulta previa (RN-07 / RN-09)
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("No se puede registrar una consulta para una cita cancelada");
        }
        if (cita.getEstado() == EstadoCita.ATENDIDA || consultaRepository.existsByCitaId(cita.getId())) {
            throw new ConsultaDuplicadaException("La cita ya tiene una consulta registrada");
        }

        // 4. La fecha de la consulta debe coincidir con la de la cita
        if (!request.getFecha().equals(cita.getFechaHora().toLocalDate())) {
            throw new IllegalArgumentException("La fecha de la consulta no coincide con la fecha de la cita");
        }

        // 5. La mascota se obtiene a través de la cita
        Mascota mascota = mascotaRepository.findById(cita.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + cita.getMascotaId()));

        // 6. Persistir la consulta
        Consulta consulta = new Consulta();
        consulta.setCitaId(cita.getId());
        consulta.setPesoKg(request.getPeso());
        consulta.setTemperaturaC(request.getTemperatura());
        consulta.setMotivo(request.getMotivo().trim());
        consulta.setDiagnostico(request.getDiagnostico().trim());
        consulta.setObservaciones(StringUtils.trimToNull(request.getObservaciones()));
        Consulta guardada = consultaRepository.save(consulta);

        // 7. RF-12: actualizar peso actual de la mascota (desnormalización controlada)
        mascota.setPesoActualKg(request.getPeso());
        mascotaRepository.save(mascota);

        // 8. La cita pasa a ATENDIDA
        cita.setEstado(EstadoCita.ATENDIDA);
        citaRepository.save(cita);

        log.info("Consulta [{}] registrada para cita [{}] y mascota [{}]. Peso actual actualizado a {} kg",
                guardada.getId(), cita.getId(), mascota.getId(), request.getPeso());

        return new ConsultaResponse(
                guardada.getId(),
                cita.getId(),
                mascota.getId(),
                mascota.getNombre(),
                veterinario.getId(),
                cita.getFechaHora().toLocalDate(),
                guardada.getMotivo(),
                guardada.getDiagnostico(),
                guardada.getPesoKg(),
                guardada.getTemperaturaC(),
                guardada.getObservaciones(),
                guardada.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public DiagnosticoResponse registrarDiagnostico(UUID consultaId, DiagnosticoRequest request) {
        Preconditions.checkNotNull(consultaId, "El ID de la consulta es requerido");
        Preconditions.checkNotNull(request, "La solicitud de diagnóstico no puede ser nula");
        Preconditions.checkNotNull(request.getDescripcion(), "La descripción del diagnóstico es obligatoria");

        // 1. La consulta debe existir (HTTP 404)
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta médica no encontrada con ID: " + consultaId));

        // 2. Solo puede haber un diagnóstico por consulta; si ya existe, retorna HTTP 409
        if (StringUtils.isNotBlank(consulta.getDiagnostico())) {
            log.warn("Rechazo de diagnóstico: la consulta [{}] ya tiene un diagnóstico registrado", consultaId);
            throw new DiagnosticoDuplicadoException("La consulta ya tiene un diagnóstico registrado");
        }

        // 3. Vincular y persistir el diagnóstico
        String descripcion = request.getDescripcion().trim();
        consulta.setDiagnostico(descripcion);
        consultaRepository.save(consulta);

        log.info("Diagnóstico registrado exitosamente para la consulta [{}]", consultaId);

        return new DiagnosticoResponse(
                consulta.getId(),
                descripcion,
                StringUtils.trimToNull(request.getCodigoCIE()),
                LocalDateTime.now()
        );
    }
}
