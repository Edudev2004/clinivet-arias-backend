package com.clinivet.arias.repository;

import com.clinivet.arias.model.Cita;
import com.clinivet.arias.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Cita}.
 * Permite validar cruces de horarios (RN-03 / RF-14 / RF-15).
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, UUID> {

    /**
     * Verifica si el veterinario ya tiene una cita registrada en una fecha y hora con un estado específico.
     */
    boolean existsByVeterinarioIdAndFechaHoraAndEstado(UUID veterinarioId, LocalDateTime fechaHora, EstadoCita estado);

    /**
     * Retorna todas las citas activas de un veterinario dentro de una fecha específica.
     */
    List<Cita> findByVeterinarioIdAndFechaHoraBetweenAndEstado(
            UUID veterinarioId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);
}
