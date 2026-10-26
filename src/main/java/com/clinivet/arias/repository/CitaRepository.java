package com.clinivet.arias.repository;

import com.clinivet.arias.model.Cita;
import com.clinivet.arias.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link Cita}.
 * Provee métodos para consultas de agenda, disponibilidad y prevención de cruces (RN-03 / RF-15).
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, UUID> {

    List<Cita> findByMascotaId(UUID mascotaId);

    List<Cita> findByVeterinarioId(UUID veterinarioId);

    List<Cita> findByVeterinarioIdAndFechaHoraBetween(UUID veterinarioId, LocalDateTime start, LocalDateTime end);

    List<Cita> findByFechaHoraBetween(LocalDateTime start, LocalDateTime end);

    List<Cita> findByEstado(EstadoCita estado);

    /**
     * Verifica si existe una cita activa para el veterinario en la misma fecha y hora (RN-03).
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cita c " +
           "WHERE c.veterinario.id = :veterinarioId " +
           "AND c.fechaHora = :fechaHora " +
           "AND c.estado <> com.clinivet.arias.model.EstadoCita.cancelada")
    boolean existsConflictoHorario(@Param("veterinarioId") UUID veterinarioId,
                                   @Param("fechaHora") LocalDateTime fechaHora);
}
