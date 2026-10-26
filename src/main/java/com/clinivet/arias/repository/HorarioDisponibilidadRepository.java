package com.clinivet.arias.repository;

import com.clinivet.arias.model.HorarioDisponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para la entidad {@link HorarioDisponibilidad}.
 * Permite consultar turnos configurados por veterinario y día de la semana (RN-11).
 */
@Repository
public interface HorarioDisponibilidadRepository extends JpaRepository<HorarioDisponibilidad, UUID> {

    List<HorarioDisponibilidad> findByVeterinarioId(UUID veterinarioId);

    List<HorarioDisponibilidad> findByVeterinarioIdAndDiaSemana(UUID veterinarioId, Integer diaSemana);
}
