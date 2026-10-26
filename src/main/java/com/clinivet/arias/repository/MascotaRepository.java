package com.clinivet.arias.repository;

import com.clinivet.arias.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la entidad {@link Mascota}.
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, UUID> {

    List<Mascota> findByPropietarioId(UUID propietarioId);

    List<Mascota> findByPropietarioIdAndActivoTrue(UUID propietarioId);

    List<Mascota> findByActivoTrue();
}
