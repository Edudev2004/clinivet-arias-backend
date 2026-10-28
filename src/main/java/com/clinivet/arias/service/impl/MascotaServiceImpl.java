package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.HistorialConsultaResponse;
import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.dto.MascotaUpdateRequest;
import com.clinivet.arias.dto.TratamientoResponse;
import com.clinivet.arias.dto.VacunaAplicadaResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.Cita;
import com.clinivet.arias.model.Consulta;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.*;
import com.clinivet.arias.service.IMascotaService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de mascotas, expedientes y su historial clínico (RF-09, RF-10, RF-11, RF-12, RF-25, RF-26).
 * Aplica principios SOLID:
 * - SRP: Exclusivamente enfocado en el ciclo de vida y reglas de pacientes/mascotas y su historial clínico inmutable (RN-09).
 * - DIP: Implementa IMascotaService y se apoya en abstracciones de persistencia Spring Data JPA.
 */
@Service
public class MascotaServiceImpl implements IMascotaService {

    private static final Logger log = LoggerFactory.getLogger(MascotaServiceImpl.class);

    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;
    private final ConsultaRepository consultaRepository;
    private final CitaRepository citaRepository;
    private final TratamientoRepository tratamientoRepository;
    private final VacunaAplicadaRepository vacunaAplicadaRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository,
                              PropietarioRepository propietarioRepository,
                              ConsultaRepository consultaRepository,
                              CitaRepository citaRepository,
                              TratamientoRepository tratamientoRepository,
                              VacunaAplicadaRepository vacunaAplicadaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.propietarioRepository = propietarioRepository;
        this.consultaRepository = consultaRepository;
        this.citaRepository = citaRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
    }

    @Override
    @Transactional
    public MascotaResponse registrar(MascotaCreateRequest request) {
        Preconditions.checkNotNull(request, "La solicitud de registro de mascota no puede ser nula");
        Preconditions.checkNotNull(request.getPropietarioId(), "El identificador del propietario es obligatorio (RN-01)");
        Preconditions.checkNotNull(request.getNombre(), "El nombre de la mascota es obligatorio");
        Preconditions.checkNotNull(request.getEspecie(), "La especie de la mascota es obligatoria");
        Preconditions.checkNotNull(request.getSexo(), "El sexo de la mascota es obligatorio");

        log.info("Iniciando registro de mascota [{}] vinculada a propietario [{}] (RF-09 / RN-01)",
                request.getNombre(), request.getPropietarioId());

        // 1. Validar que el propietario exista obligatoriamente (RN-01 / HTTP 400 si no existe)
        Propietario propietario = propietarioRepository.findById(request.getPropietarioId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El propietario con ID [" + request.getPropietarioId() + "] no existe en la base de datos (RN-01)"));

        // 2. Limpieza defensiva y normalización
        String nombre = StringUtils.trim(request.getNombre());
        String especie = StringUtils.trim(request.getEspecie());
        String raza = StringUtils.trimToNull(request.getRaza());
        String color = StringUtils.trimToNull(request.getColor());
        String fotoUrl = StringUtils.trimToNull(request.getFotoUrl());

        String sexoRaw = StringUtils.trim(request.getSexo());
        String sexo = normalizarSexo(sexoRaw);

        // 3. Crear entidad Mascota
        Mascota nuevaMascota = new Mascota(
                null,
                propietario,
                nombre,
                especie,
                raza,
                color,
                sexo,
                request.getFechaNacimiento(),
                request.getPesoActualKg(),
                true
        );
        nuevaMascota.setFotoUrl(fotoUrl);

        Mascota guardada = mascotaRepository.save(nuevaMascota);

        log.info("Mascota [{}] de especie [{}] registrada exitosamente con ID [{}] vinculada al propietario [{} {}] (RF-09)",
                guardada.getNombre(), guardada.getEspecie(), guardada.getId(),
                propietario.getNombre(), propietario.getApellido());

        return mapearAResponse(guardada, propietario);
    }

    @Override
    @Transactional(readOnly = true)
    public MascotaResponse obtenerPorId(UUID id) {
        Preconditions.checkNotNull(id, "El identificador de la mascota no puede ser nulo");

        log.debug("Consultando ficha completa de mascota con ID [{}] (RF-10)", id);

        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + id));

        // Criterio de aceptación RF-10: El peso mostrado es el más reciente registrado en cualquier consulta médica
        BigDecimal pesoMasReciente = mascota.getPesoActualKg();
        if (consultaRepository != null) {
            List<BigDecimal> pesosConsultas = consultaRepository.findPesosRecientesPorMascotaId(id);
            if (pesosConsultas != null && !pesosConsultas.isEmpty() && pesosConsultas.get(0) != null) {
                pesoMasReciente = pesosConsultas.get(0);
                log.debug("Se obtuvo peso más reciente [{}] desde consultas médicas para la mascota [{}]", pesoMasReciente, id);
            }
        }

        MascotaResponse response = mapearAResponse(mascota, mascota.getPropietario());
        response.setPesoActualKg(pesoMasReciente);
        return response;
    }

    @Override
    @Transactional
    public MascotaResponse actualizar(UUID id, MascotaUpdateRequest request) {
        Preconditions.checkNotNull(id, "El identificador de la mascota no puede ser nulo");
        Preconditions.checkNotNull(request, "La solicitud de actualización no puede ser nula");
        Preconditions.checkNotNull(request.getNombre(), "El nombre de la mascota es obligatorio");
        Preconditions.checkNotNull(request.getEspecie(), "La especie de la mascota es obligatoria");
        Preconditions.checkNotNull(request.getSexo(), "El sexo de la mascota es obligatorio");

        log.info("Iniciando actualización de datos de la mascota [{}] (RF-11)", id);

        // 1. Obtener mascota existente o lanzar 404
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + id));

        // 2. Limpieza defensiva y normalización
        String nombre = StringUtils.trim(request.getNombre());
        String especie = StringUtils.trim(request.getEspecie());
        String raza = StringUtils.trimToNull(request.getRaza());
        String color = StringUtils.trimToNull(request.getColor());
        String fotoUrl = StringUtils.trimToNull(request.getFotoUrl());
        String sexo = normalizarSexo(StringUtils.trim(request.getSexo()));

        // 3. Modificar campos permitidos (el vínculo con el propietario es permanente y no se altera)
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setColor(color);
        mascota.setSexo(sexo);
        mascota.setFechaNacimiento(request.getFechaNacimiento());
        if (request.getPesoActualKg() != null) {
            mascota.setPesoActualKg(request.getPesoActualKg());
        }
        mascota.setFotoUrl(fotoUrl);
        if (request.getActivo() != null) {
            mascota.setActivo(request.getActivo());
        }

        Mascota actualizada = mascotaRepository.save(mascota);

        log.info("Mascota [{}] (ID: [{}]) actualizada con éxito manteniendo propietario [{}]",
                actualizada.getNombre(), actualizada.getId(), actualizada.getPropietario().getId());

        return mapearAResponse(actualizada, actualizada.getPropietario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MascotaResponse> listarTodas() {
        log.debug("Consultando listado de todas las mascotas activas (RF-10 / RF-12)");
        List<Mascota> mascotas = mascotaRepository.findByActivoTrue();
        return mascotas.stream()
                .map(m -> {
                    BigDecimal pesoMasReciente = m.getPesoActualKg();
                    if (consultaRepository != null) {
                        List<BigDecimal> pesos = consultaRepository.findPesosRecientesPorMascotaId(m.getId());
                        if (pesos != null && !pesos.isEmpty() && pesos.get(0) != null) {
                            pesoMasReciente = pesos.get(0);
                        }
                    }
                    MascotaResponse res = mapearAResponse(m, m.getPropietario());
                    res.setPesoActualKg(pesoMasReciente);
                    return res;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialConsultaResponse> obtenerHistorial(
            UUID mascotaId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            UUID veterinarioId,
            Pageable pageable) {
        log.debug("Consultando historial clínico de la mascota [{}] con filtros (RF-25 / RF-26)", mascotaId);

        if (!mascotaRepository.existsById(mascotaId)) {
            throw new ResourceNotFoundException("Mascota no encontrada con ID: " + mascotaId);
        }

        LocalDateTime inicioDateTime = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime finDateTime = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        Page<Consulta> consultasPage = consultaRepository.findHistorialPorMascota(
                mascotaId,
                veterinarioId,
                inicioDateTime,
                finDateTime,
                pageable
        );

        return consultasPage.map(consulta -> {
            Cita cita = citaRepository.findById(consulta.getCitaId()).orElse(null);
            LocalDateTime fechaHora = (cita != null) ? cita.getFechaHora() : consulta.getCreatedAt();
            UUID vetId = (cita != null && cita.getVeterinario() != null) ? cita.getVeterinario().getId() : null;
            String vetNombre = (cita != null && cita.getVeterinario() != null)
                    ? cita.getVeterinario().getNombreCompleto().trim() : "";

            // Tratamientos prescritos en la consulta (RF-22 / RF-23)
            List<TratamientoResponse> tratamientos = tratamientoRepository.findByConsultaIdOrderByCreatedAtAsc(consulta.getId())
                    .stream()
                    .map(t -> new TratamientoResponse(
                            t.getId(),
                            consulta.getId(),
                            t.getMedicamento() != null ? t.getMedicamento().getId() : null,
                            t.getMedicamento() != null ? t.getMedicamento().getNombre() : "Medicamento",
                            t.getMedicamento() != null ? t.getMedicamento().getPrincipioActivo() : null,
                            consulta.getPesoKg(),
                            t.getDosisMgPorKg(),
                            t.getDosisTotalMg(),
                            t.getDuracionDias(),
                            t.getIndicaciones(),
                            t.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            // Vacunas aplicadas en la consulta (RF-24)
            List<VacunaAplicadaResponse> vacunas = vacunaAplicadaRepository.findByConsultaId(consulta.getId())
                    .stream()
                    .map(v -> new VacunaAplicadaResponse(
                            v.getId(),
                            v.getMascota().getId(),
                            v.getMascota().getNombre(),
                            v.getVacuna().getId(),
                            v.getVacuna().getNombre(),
                            v.getFechaAplicacion(),
                            v.getProximaDosis(),
                            v.getVeterinario().getId(),
                            v.getVeterinario().getNombreCompleto().trim(),
                            v.getLote(),
                            consulta.getId(),
                            v.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            return new HistorialConsultaResponse(
                    consulta.getId(),
                    consulta.getCitaId(),
                    fechaHora,
                    vetId,
                    vetNombre,
                    consulta.getMotivo(),
                    consulta.getDiagnostico(),
                    consulta.getPesoKg(),
                    consulta.getTemperaturaC(),
                    consulta.getObservaciones(),
                    tratamientos,
                    vacunas,
                    consulta.getCreatedAt()
            );
        });
    }

    private String normalizarSexo(String sexoRaw) {
        if (sexoRaw == null) {
            return "Macho";
        }
        if (sexoRaw.equalsIgnoreCase("M") || sexoRaw.equalsIgnoreCase("Macho")) {
            return "Macho";
        }
        if (sexoRaw.equalsIgnoreCase("H") || sexoRaw.equalsIgnoreCase("Hembra")) {
            return "Hembra";
        }
        return sexoRaw;
    }

    private MascotaResponse mapearAResponse(Mascota m, Propietario p) {
        String propietarioNombre = p != null ? (p.getNombre() + " " + p.getApellido()) : null;
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
                p != null ? p.getId() : null,
                propietarioNombre,
                m.getCreatedAt()
        );
    }
}
