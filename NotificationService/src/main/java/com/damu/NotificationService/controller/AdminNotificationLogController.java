package com.damu.NotificationService.controller;

import com.damu.NotificationService.model.ApiResponse;
import com.damu.NotificationService.model.NotificationLog;
import com.damu.NotificationService.service.DeliveryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notification/logs")
public class AdminNotificationLogController {

    private final DeliveryLogService deliveryLogService;

    @GetMapping("/event/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NotificationLog>> findByEventId(@PathVariable String eventId) {
        return ApiResponse.ok("Notification logs fetched", deliveryLogService.findByEventId(eventId));
    }

    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NotificationLog>> findByUserId(@PathVariable String userId) {
        return ApiResponse.ok("Notification logs fetched", deliveryLogService.findByUserId(userId));
    }
}
