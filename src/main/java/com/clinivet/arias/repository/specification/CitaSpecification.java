package com.clinivet.arias.repository.specification;

import com.clinivet.arias.model.Cita;
import com.clinivet.arias.model.EstadoCita;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Especificación JPA dinámica para el filtrado flexible y combinable de citas médicas (RF-18).
 * Soporta filtros opcionales por veterinario, fecha puntual, rango de fechas y estado de la cita.
 */
public class CitaSpecification {

    private CitaSpecification() {
        // Constructor privado para clase utilitaria
    }

    /**
     * Construye una especificación JPA combinando dinámicamente los predicados no nulos.
     *
     * @param veterinarioId identificador opcional del médico veterinario
     * @param fecha fecha exacta opcional para filtrar citas de ese día completo
     * @param fechaInicio fecha inicial opcional para rango de búsqueda
     * @param fechaFin fecha final opcional para rango de búsqueda
     * @param estado estado opcional de la cita (PROGRAMADA, ATENDIDA, CANCELADA)
     * @return Specification combinada
     */
    public static Specification<Cita> conFiltros(UUID veterinarioId,
                                                LocalDate fecha,
                                                LocalDate fechaInicio,
                                                LocalDate fechaFin,
                                                EstadoCita estado) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filtro por veterinario
            if (veterinarioId != null) {
                predicates.add(cb.equal(root.get("veterinario").get("id"), veterinarioId));
            }

            // 2. Filtro por fecha o rango de fechas
            if (fecha != null) {
                LocalDateTime inicioDia = fecha.atStartOfDay();
                LocalDateTime finDia = fecha.atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("fechaHora"), inicioDia, finDia));
            } else {
                if (fechaInicio != null && fechaFin != null) {
                    predicates.add(cb.between(root.get("fechaHora"), fechaInicio.atStartOfDay(), fechaFin.atTime(LocalTime.MAX)));
                } else if (fechaInicio != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), fechaInicio.atStartOfDay()));
                } else if (fechaFin != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), fechaFin.atTime(LocalTime.MAX)));
                }
            }

            // 3. Filtro por estado
            if (estado != null) {
                predicates.add(cb.equal(root.get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
