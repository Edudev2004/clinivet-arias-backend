package com.clinivet.arias.repository;

import com.clinivet.arias.model.VacunaCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para el catálogo de vacunas (Tabla 31).
 */
@Repository
public interface VacunaCatalogoRepository extends JpaRepository<VacunaCatalogo, UUID> {

    Optional<VacunaCatalogo> findByNombreIgnoreCase(String nombre);
}
