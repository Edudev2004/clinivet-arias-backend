package com.clinivet.arias.repository;

import com.clinivet.arias.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Veterinario}.
 */
@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, UUID> {

    Optional<Veterinario> findByUsuarioId(UUID usuarioId);

    List<Veterinario> findByActivoTrue();
}
