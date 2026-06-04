package com.damu.NotificationService.service.impl;

import com.damu.NotificationService.dto.DeliveryResult;
import com.damu.NotificationService.dto.SendNotificationCommand;
import com.damu.NotificationService.model.DeliveryAttempt;
import com.damu.NotificationService.model.NotificationLog;
import com.damu.NotificationService.model.NotificationStatus;
import com.damu.NotificationService.repository.NotificationLogRepository;
import com.damu.NotificationService.service.DeliveryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryLogServiceImpl implements DeliveryLogService {

    private final NotificationLogRepository repository;

    @Override
    public NotificationLog createPending(SendNotificationCommand command) {
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
            return repository.save(log);
        } catch (DuplicateKeyException exception) {
            return repository.findByEventIdAndUserIdAndChannel(command.eventId(), command.userId(), command.channel())
                    .orElseThrow(() -> exception);
        }
    }

    @Override
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
