package com.clinivet.arias.service.impl;

import com.clinivet.arias.dto.DisponibilidadResponse;
import com.clinivet.arias.exception.ResourceNotFoundException;
import com.clinivet.arias.model.EstadoCita;
import com.clinivet.arias.model.HorarioDisponibilidad;
import com.clinivet.arias.model.Veterinario;
import com.clinivet.arias.repository.CitaRepository;
import com.clinivet.arias.repository.HorarioDisponibilidadRepository;
import com.clinivet.arias.repository.VeterinarioRepository;
import com.clinivet.arias.service.IVeterinarioService;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación de la lógica de negocio para la gestión de veterinarios y control de agenda.
 * Aplica principios SOLID:
 * - SRP: Exclusivamente responsable de validar la disponibilidad de especialistas según turnos y citas.
 * - DIP: Inyecta interfaces de repositorio (VeterinarioRepository, HorarioDisponibilidadRepository, CitaRepository).
 * - Uso estricto de Google Guava Preconditions.
 * Cumple con RN-03 (prevención de cruces), RN-11 (horarios de disponibilidad) y RF-14 según APF2.
 */
@Service
public class VeterinarioServiceImpl implements IVeterinarioService {

    private static final Logger log = LoggerFactory.getLogger(VeterinarioServiceImpl.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final VeterinarioRepository veterinarioRepository;
    private final HorarioDisponibilidadRepository horarioDisponibilidadRepository;
    private final CitaRepository citaRepository;

    // Inyección de dependencias por constructor (DIP)
    public VeterinarioServiceImpl(VeterinarioRepository veterinarioRepository,
                                  HorarioDisponibilidadRepository horarioDisponibilidadRepository,
                                  CitaRepository citaRepository) {
        this.veterinarioRepository = veterinarioRepository;
        this.horarioDisponibilidadRepository = horarioDisponibilidadRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponse consultarDisponibilidad(UUID veterinarioId, LocalDate fecha, LocalTime hora) {
        Preconditions.checkNotNull(veterinarioId, "El identificador del veterinario no puede ser nulo");
        Preconditions.checkNotNull(fecha, "La fecha no puede ser nula");
        Preconditions.checkNotNull(hora, "La hora no puede ser nula");

        // 1. Validar existencia y estado del veterinario
        Veterinario veterinario = veterinarioRepository.findById(veterinarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Médico veterinario no encontrado con ID: " + veterinarioId));

        if (Boolean.FALSE.equals(veterinario.getActivo())) {
            throw new IllegalArgumentException("El médico veterinario seleccionado se encuentra inactivo");
        }

        String nombreCompleto = "Dr. " + veterinario.getUsuario().getNombre() + " " + veterinario.getUsuario().getApellido();
        String especialidadNombre = veterinario.getEspecialidad().getNombre();
        String colegiatura = veterinario.getNumeroColegiatura();

        // 2. Obtener día de la semana según convención de APF2: 0=Domingo, 1=Lunes, ..., 6=Sábado
        int diaSemana = fecha.getDayOfWeek().getValue() % 7;

        // 3. Consultar horarios configurados para ese día (RN-11)
        List<HorarioDisponibilidad> turnos = horarioDisponibilidadRepository
                .findByVeterinarioIdAndDiaSemana(veterinarioId, diaSemana);

        List<String> turnosFormateados = turnos.stream()
                .map(t -> t.getHoraInicio().format(TIME_FMT) + " - " + t.getHoraFin().format(TIME_FMT))
                .collect(Collectors.toList());

        // 4. Evaluar si la hora se encuentra dentro de alguna franja configurada (RN-11)
        boolean dentroDeTurno = turnos.stream().anyMatch(t -> t.incluyeHora(hora));

        if (!dentroDeTurno) {
            String mensaje = turnos.isEmpty()
                    ? "El especialista no atiende los días " + obtenerNombreDia(diaSemana) + " (RN-11)"
                    : "La hora solicitada (" + hora.format(TIME_FMT) + ") está fuera del horario de atención del especialista para este día (RN-11)";

            log.info("Disponibilidad negativa para veterinario [{}] en {} {}: Fuera de turno",
                    veterinarioId, fecha, hora);

            return new DisponibilidadResponse(
                    veterinarioId, nombreCompleto, colegiatura, especialidadNombre,
                    fecha, hora, false, mensaje, turnosFormateados
            );
        }

        // 5. Evaluar si ya existe una cita médica activa a la misma fecha y hora (RN-03 / RF-15)
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
        boolean tieneCitaOcupada = citaRepository
                .existsByVeterinarioIdAndFechaHoraAndEstado(veterinarioId, fechaHora, EstadoCita.PROGRAMADA);

        if (tieneCitaOcupada) {
            String mensaje = "El especialista ya cuenta con una cita médica programada en esa fecha y hora (RN-03 / RF-15)";
            log.info("Disponibilidad negativa para veterinario [{}] en {}: Cruce de cita existente",
                    veterinarioId, fechaHora);

            return new DisponibilidadResponse(
                    veterinarioId, nombreCompleto, colegiatura, especialidadNombre,
                    fecha, hora, false, mensaje, turnosFormateados
            );
        }

        // 6. Horario 100% disponible
        log.info("Veterinario [{}] disponible exitosamente para {} {}", veterinarioId, fecha, hora);
        return new DisponibilidadResponse(
                veterinarioId, nombreCompleto, colegiatura, especialidadNombre,
                fecha, hora, true, "Horario disponible para agendar atención médica", turnosFormateados
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esHorarioDisponible(UUID veterinarioId, LocalDateTime fechaHora) {
        if (veterinarioId == null || fechaHora == null) {
            return false;
        }

        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime hora = fechaHora.toLocalTime();

        try {
            DisponibilidadResponse disp = consultarDisponibilidad(veterinarioId, fecha, hora);
            return disp.isDisponible();
        } catch (Exception e) {
            log.warn("Error al evaluar disponibilidad para veterinario [{}] en [{}]: {}",
                    veterinarioId, fechaHora, e.getMessage());
            return false;
        }
    }

    private String obtenerNombreDia(int diaSemana) {
        return switch (diaSemana) {
            case 0 -> "Domingo";
            case 1 -> "Lunes";
            case 2 -> "Martes";
            case 3 -> "Miércoles";
            case 4 -> "Jueves";
            case 5 -> "Viernes";
            case 6 -> "Sábado";
            default -> "Día " + diaSemana;
        };
    }
}
