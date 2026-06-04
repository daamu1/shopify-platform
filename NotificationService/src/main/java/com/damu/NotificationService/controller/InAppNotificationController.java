package com.damu.NotificationService.controller;

import com.damu.NotificationService.model.ApiResponse;
import com.damu.NotificationService.model.InAppNotification;
import com.damu.NotificationService.service.InAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications/in-app")
public class InAppNotificationController {

    private final InAppNotificationService service;

    @GetMapping


    public ApiResponse<List<InAppNotification>> findByUser(@RequestParam String userId) {
        return ApiResponse.ok("In-app notifications fetched", service.findByUser(userId));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<InAppNotification> markRead(@PathVariable String id) {
        return ApiResponse.ok("Notification marked as read", service.markRead(id));
    }
}
