package com.clinivet.arias.repository;

import com.clinivet.arias.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Consulta}.
 * Soporta consulta por cita y por id con identificador UUID.
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    Optional<Consulta> findByCitaId(UUID citaId);

    boolean existsByCitaId(UUID citaId);
}
