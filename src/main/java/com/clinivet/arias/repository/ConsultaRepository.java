package com.clinivet.arias.repository;

import com.clinivet.arias.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Consulta}.
 * Soporta consulta por cita y por id con identificador UUID.
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    Optional<Consulta> findByCitaId(UUID citaId);

    @Query("""
        SELECT c.pesoKg FROM Consulta c, Cita ci
        WHERE c.citaId = ci.id
          AND ci.mascotaId = :mascotaId
          AND c.pesoKg IS NOT NULL
        ORDER BY c.createdAt DESC
    """)
    List<BigDecimal> findPesosRecientesPorMascotaId(@Param("mascotaId") UUID mascotaId);
}
