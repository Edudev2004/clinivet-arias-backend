package com.clinivet.arias.repository;

import com.clinivet.arias.model.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Especialidad}.
 */
@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, UUID> {

    Optional<Especialidad> findByNombre(String nombre);
}
