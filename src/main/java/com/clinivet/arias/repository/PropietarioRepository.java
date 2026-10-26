package com.clinivet.arias.repository;

import com.clinivet.arias.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link Propietario}.
 */
@Repository
public interface PropietarioRepository extends JpaRepository<Propietario, UUID> {

    Optional<Propietario> findByDni(String dni);

    List<Propietario> findByActivoTrue();

    List<Propietario> findByApellidoContainingIgnoreCaseOrNombreContainingIgnoreCase(String apellido, String nombre);
}
