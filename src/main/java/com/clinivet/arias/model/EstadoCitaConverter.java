package com.clinivet.arias.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Conversor JPA para mapear {@link EstadoCita} a la representación en minúsculas de PostgreSQL.
 */
@Converter(autoApply = true)
public class EstadoCitaConverter implements AttributeConverter<EstadoCita, String> {

    @Override
    public String convertToDatabaseColumn(EstadoCita attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCodigo();
    }

    @Override
    public EstadoCita convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EstadoCita.fromCodigo(dbData);
    }
}
