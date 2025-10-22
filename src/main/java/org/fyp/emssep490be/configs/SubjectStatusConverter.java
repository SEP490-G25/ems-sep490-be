package org.fyp.emssep490be.configs;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.fyp.emssep490be.entities.enums.SubjectStatus;

/**
 * JPA Converter to handle SubjectStatus enum
 * Converts Java enum (UPPERCASE) to database value (lowercase)
 * This is needed because the database check constraint expects lowercase values
 */
@Converter(autoApply = true)
public class SubjectStatusConverter implements AttributeConverter<SubjectStatus, String> {

    @Override
    public String convertToDatabaseColumn(SubjectStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public SubjectStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return SubjectStatus.valueOf(dbData.toUpperCase());
    }
}
