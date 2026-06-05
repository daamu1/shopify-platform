package com.damu.notificationservice.service.impl;

import com.damu.notificationservice.dto.TemplateRequest;
import com.damu.notificationservice.exception.ResourceNotFoundException;
import com.damu.notificationservice.model.NotificationChannel;
import com.damu.notificationservice.model.NotificationTemplate;
import com.damu.notificationservice.repository.NotificationTemplateRepository;
import com.damu.notificationservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository repository;

    @Override
    public NotificationTemplate create(TemplateRequest request) {
        Instant now = Instant.now();
        int version = repository.findByEventTypeAndChannelOrderByVersionDesc(request.eventType(), request.channel())
                .stream()
                .findFirst()
                .map(template -> template.getVersion() + 1)
                .orElse(1);

        NotificationTemplate template = NotificationTemplate.builder()
                .eventType(request.eventType())
                .channel(request.channel())
                .name(request.name())
                .locale(request.locale() == null ? "en" : request.locale())
                .version(version)
                .active(Boolean.TRUE.equals(request.active()))
                .subjectTemplate(request.subjectTemplate())
                .bodyTemplate(request.bodyTemplate())
                .requiredVariables(request.requiredVariables())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return repository.save(template);
    }

    @Override
    public List<NotificationTemplate> findAll(String eventType, NotificationChannel channel) {
        if (eventType != null && channel != null) {
            return repository.findByEventTypeAndChannelOrderByVersionDesc(eventType, channel);
        }
        if (eventType != null) {
            return repository.findByEventTypeOrderByVersionDesc(eventType);
        }
        return repository.findAll();
    }

    @Override
    public NotificationTemplate getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
    }

    @Override
    public NotificationTemplate getActiveTemplate(String eventType, NotificationChannel channel) {
        return repository.findFirstByEventTypeAndChannelAndActiveTrueOrderByVersionDesc(eventType, channel)
                .orElseThrow(() -> new ResourceNotFoundException("Active template not found for " + eventType + " " + channel));
    }

    @Override
    public NotificationTemplate update(String id, TemplateRequest request) {
        NotificationTemplate template = getById(id);
        template.setEventType(request.eventType());
        template.setChannel(request.channel());
        template.setName(request.name());
        template.setLocale(request.locale() == null ? template.getLocale() : request.locale());
        template.setActive(Boolean.TRUE.equals(request.active()));
        template.setSubjectTemplate(request.subjectTemplate());
        template.setBodyTemplate(request.bodyTemplate());
        template.setRequiredVariables(request.requiredVariables());
        template.setUpdatedAt(Instant.now());
        return repository.save(template);
    }

    @Override
    public NotificationTemplate activate(String id) {
        NotificationTemplate selected = getById(id);
        List<NotificationTemplate> templates = repository.findByEventTypeAndChannelOrderByVersionDesc(
                selected.getEventType(), selected.getChannel());
        for (NotificationTemplate template : templates) {
            template.setActive(template.getId().equals(id));
            template.setUpdatedAt(Instant.now());
        }
        repository.saveAll(templates);
        return getById(id);
    }
}
