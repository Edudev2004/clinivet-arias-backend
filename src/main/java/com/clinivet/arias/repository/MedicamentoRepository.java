package com.clinivet.arias.repository;

import com.clinivet.arias.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link Medicamento}.
 * Permite consultar medicamentos activos y por nombre.
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, UUID> {

    List<Medicamento> findByActivoTrue();
}
