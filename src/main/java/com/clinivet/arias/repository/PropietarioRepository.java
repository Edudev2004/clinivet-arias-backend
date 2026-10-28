package com.clinivet.arias.repository;

import com.clinivet.arias.model.Propietario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Page<Propietario> findByActivoTrue(Pageable pageable);

    List<Propietario> findByApellidoContainingIgnoreCaseOrNombreContainingIgnoreCase(String apellido, String nombre);

    @Query("SELECT p FROM Propietario p WHERE p.activo = true AND (" +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "p.dni LIKE CONCAT('%', :query, '%') OR " +
            "p.telefono LIKE CONCAT('%', :query, '%'))")
    Page<Propietario> buscarPorTermino(@Param("query") String query, Pageable pageable);
}
