package com.clinivet.arias.repository;

import com.clinivet.arias.model.VacunaAplicada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para el historial de vacunas aplicadas (RF-24).
 */
@Repository
public interface VacunaAplicadaRepository extends JpaRepository<VacunaAplicada, UUID> {

    List<VacunaAplicada> findByMascotaIdOrderByFechaAplicacionDesc(UUID mascotaId);

    List<VacunaAplicada> findByConsultaId(UUID consultaId);
}
