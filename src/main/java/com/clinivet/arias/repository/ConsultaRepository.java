package com.clinivet.arias.repository;

import com.clinivet.arias.model.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Consulta}.
 * Soporta consulta por cita, historial por mascota con filtros y cálculo de pesos (RF-12, RF-25, RF-26).
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    Optional<Consulta> findByCitaId(UUID citaId);

    boolean existsByCitaId(UUID citaId);

    @Query("""
        SELECT c.pesoKg FROM Consulta c, Cita ci
        WHERE c.citaId = ci.id
          AND ci.mascotaId = :mascotaId
          AND c.pesoKg IS NOT NULL
        ORDER BY c.createdAt DESC
    """)
    List<BigDecimal> findPesosRecientesPorMascotaId(@Param("mascotaId") UUID mascotaId);

    @Query(value = """
        SELECT c FROM Consulta c, Cita ci
        WHERE c.citaId = ci.id
          AND ci.mascotaId = :mascotaId
          AND (:veterinarioId IS NULL OR ci.veterinario.id = :veterinarioId)
          AND (CAST(:fechaInicio AS java.time.LocalDateTime) IS NULL OR ci.fechaHora >= :fechaInicio)
          AND (CAST(:fechaFin AS java.time.LocalDateTime) IS NULL OR ci.fechaHora <= :fechaFin)
        ORDER BY ci.fechaHora DESC
    """,
    countQuery = """
        SELECT COUNT(c) FROM Consulta c, Cita ci
        WHERE c.citaId = ci.id
          AND ci.mascotaId = :mascotaId
          AND (:veterinarioId IS NULL OR ci.veterinario.id = :veterinarioId)
          AND (CAST(:fechaInicio AS java.time.LocalDateTime) IS NULL OR ci.fechaHora >= :fechaInicio)
          AND (CAST(:fechaFin AS java.time.LocalDateTime) IS NULL OR ci.fechaHora <= :fechaFin)
    """)
    Page<Consulta> findHistorialPorMascota(
            @Param("mascotaId") UUID mascotaId,
            @Param("veterinarioId") UUID veterinarioId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable
    );
}
