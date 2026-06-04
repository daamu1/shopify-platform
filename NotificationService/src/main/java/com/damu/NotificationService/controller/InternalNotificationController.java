package com.damu.NotificationService.controller;

import com.damu.NotificationService.dto.EventPublishRequest;
import com.damu.NotificationService.dto.NotificationEvent;
import com.damu.NotificationService.dto.SendNotificationRequest;
import com.damu.NotificationService.model.ApiResponse;
import com.damu.NotificationService.service.EventPublisherService;
import com.damu.NotificationService.service.NotificationOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final EventPublisherService eventPublisherService;
    private final NotificationOrchestrator notificationOrchestrator;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<NotificationEvent> publishEvent(@Valid @RequestBody EventPublishRequest request) {
        return ApiResponse.ok("Notification event accepted", HttpStatus.ACCEPTED.value(), eventPublisherService.publish(request));
    }

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> sendDirect(@Valid @RequestBody SendNotificationRequest request) {
        notificationOrchestrator.sendDirect(request);
        return ApiResponse.ok("Notification send accepted", HttpStatus.ACCEPTED.value(), null);
    }
}
