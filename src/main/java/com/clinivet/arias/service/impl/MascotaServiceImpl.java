package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.MascotaCreateRequest;
import com.clinivet.arias.dto.MascotaResponse;
import com.clinivet.arias.model.Mascota;
import com.clinivet.arias.model.Propietario;
import com.clinivet.arias.repository.MascotaRepository;
import com.clinivet.arias.repository.PropietarioRepository;
import com.clinivet.arias.service.IMascotaService;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de la lógica de negocio para la gestión de mascotas y pacientes veterinarios (RF-09).
 * Aplica principios SOLID:
 * - SRP: Exclusivamente enfocado en el ciclo de vida y reglas de pacientes/mascotas.
 * - DIP: Implementa IMascotaService y se apoya en abstracciones de persistencia Spring Data JPA.
 */
@Service
public class MascotaServiceImpl implements IMascotaService {

    private static final Logger log = LoggerFactory.getLogger(MascotaServiceImpl.class);

    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository, PropietarioRepository propietarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.propietarioRepository = propietarioRepository;
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
