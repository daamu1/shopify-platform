package com.damu.NotificationService.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;

@Converter
public class ChannelPreferenceConverter implements AttributeConverter<Map<NotificationChannel, Boolean>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<NotificationChannel, Boolean>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<NotificationChannel, Boolean> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public Map<NotificationChannel, Boolean> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new EnumMap<>(NotificationChannel.class);
        }
        try {
            Map<NotificationChannel, Boolean> channels = OBJECT_MAPPER.readValue(dbData, TYPE);
            if (channels == null || channels.isEmpty()) {
                return new EnumMap<>(NotificationChannel.class);
            }
            return new EnumMap<>(channels);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
