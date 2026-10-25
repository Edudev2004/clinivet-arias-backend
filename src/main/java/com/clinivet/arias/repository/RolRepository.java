package com.clinivet.arias.repository;

import com.clinivet.arias.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Rol}.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {

    /**
     * Busca un rol por su nombre único (ej. ADMINISTRADOR, VETERINARIO, RECEPCIONISTA).
     *
     * @param nombre nombre del rol
     * @return Optional con el rol si existe
     */
    Optional<Rol> findByNombre(String nombre);
}
