package com.clinivet.arias.repository;

import com.clinivet.arias.model.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Tratamiento}.
 * Soporta consulta de tratamientos prescritos por consulta.
 */
@Repository
public interface TratamientoRepository extends JpaRepository<Tratamiento, UUID> {

    List<Tratamiento> findByConsultaIdOrderByCreatedAtAsc(UUID consultaId);
}
