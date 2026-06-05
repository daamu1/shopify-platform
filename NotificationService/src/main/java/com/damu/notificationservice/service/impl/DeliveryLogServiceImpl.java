package com.damu.notificationservice.service.impl;

import com.damu.notificationservice.dto.DeliveryResult;
import com.damu.notificationservice.dto.SendNotificationCommand;
import com.damu.notificationservice.model.DeliveryAttempt;
import com.damu.notificationservice.model.NotificationLog;
import com.damu.notificationservice.model.NotificationStatus;
import com.damu.notificationservice.repository.NotificationLogRepository;
import com.damu.notificationservice.service.DeliveryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryLogServiceImpl implements DeliveryLogService {

    private final NotificationLogRepository repository;

    @Override
    public NotificationLog createPending(SendNotificationCommand command) {
        return repository.findByEventIdAndUserIdAndChannel(command.eventId(), command.userId(), command.channel())
                .orElseGet(() -> createNewPending(command));
    }

    private NotificationLog createNewPending(SendNotificationCommand command) {
        Instant now = Instant.now();
        NotificationLog log = NotificationLog.builder()
                .eventId(command.eventId())
                .eventType(command.eventType())
                .userId(command.userId())
                .channel(command.channel())
                .status(NotificationStatus.PENDING)
                .recipient(command.recipient())
                .subject(command.subject())
                .body(command.body())
                .payload(command.data())
                .attemptCount(0)
                .attempts(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
        try {
            return repository.saveAndFlush(log);
        } catch (DataIntegrityViolationException exception) {
            return repository.findByEventIdAndUserIdAndChannel(command.eventId(), command.userId(), command.channel())
                    .orElseThrow(() -> exception);
        }
    }

    @Override
    @Transactional
    public NotificationLog markSent(SendNotificationCommand command, DeliveryResult result) {
        NotificationLog log = repository.findByEventIdAndUserIdAndChannel(command.eventId(), command.userId(), command.channel())
                .orElseThrow();
        Instant now = Instant.now();
        log.setStatus(result.status());
        log.setProvider(result.provider());
        log.setProviderMessageId(result.providerMessageId());
        log.setAttemptCount(command.attemptNumber());
        log.setSentAt(now);
        log.setUpdatedAt(now);
        log.getAttempts().add(DeliveryAttempt.builder()
                .attemptNumber(command.attemptNumber())
                .status(result.status())
                .provider(result.provider())
                .attemptedAt(now)
                .build());
        return repository.save(log);
    }

    @Override
    @Transactional
    public NotificationLog markFailed(SendNotificationCommand command, Throwable error) {
        NotificationLog log = repository.findByEventIdAndUserIdAndChannel(command.eventId(), command.userId(), command.channel())
                .orElseThrow();
        Instant now = Instant.now();
        log.setStatus(NotificationStatus.FAILED);
        log.setErrorMessage(error.getMessage());
        log.setAttemptCount(command.attemptNumber());
        log.setUpdatedAt(now);
        log.getAttempts().add(DeliveryAttempt.builder()
                .attemptNumber(command.attemptNumber())
                .status(NotificationStatus.FAILED)
                .errorMessage(error.getMessage())
                .attemptedAt(now)
                .build());
        return repository.save(log);
    }

    @Override
    public List<NotificationLog> findByEventId(String eventId) {
        return repository.findByEventId(eventId);
    }

    @Override
    public List<NotificationLog> findByUserId(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
