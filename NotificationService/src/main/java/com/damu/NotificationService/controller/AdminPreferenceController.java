package com.damu.NotificationService.controller;

import com.damu.NotificationService.dto.PreferenceRequest;
import com.damu.NotificationService.model.ApiResponse;
import com.damu.NotificationService.model.UserPreference;
import com.damu.NotificationService.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notification/preferences")
public class AdminPreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<UserPreference>> findByUser(@PathVariable String userId) {
        return ApiResponse.ok("Preferences fetched", preferenceService.findByUser(userId));
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserPreference> upsert(@PathVariable String userId, @Valid @RequestBody PreferenceRequest request) {
        PreferenceRequest normalized = new PreferenceRequest(userId, request.eventType(), request.channels());
        return ApiResponse.ok("Preference saved", preferenceService.upsert(normalized));
    }
}
