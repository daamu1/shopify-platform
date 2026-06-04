package com.damu.NotificationService.controller;

import com.damu.NotificationService.dto.TemplateRequest;
import com.damu.NotificationService.model.ApiResponse;
import com.damu.NotificationService.model.NotificationChannel;
import com.damu.NotificationService.model.NotificationTemplate;
import com.damu.NotificationService.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notification/templates")
public class AdminTemplateController {

    private final TemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NotificationTemplate> create(@Valid @RequestBody TemplateRequest request) {
        return ApiResponse.ok("Template created", HttpStatus.CREATED.value(), templateService.create(request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NotificationTemplate>> findAll(@RequestParam(required = false) String eventType, @RequestParam(required = false) NotificationChannel channel) {
        return ApiResponse.ok("Templates fetched", templateService.findAll(eventType, channel));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationTemplate> getById(@PathVariable String id) {
        return ApiResponse.ok("Template fetched", templateService.getById(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationTemplate> update(@PathVariable String id, @Valid @RequestBody TemplateRequest request) {
        return ApiResponse.ok("Template updated", templateService.update(id, request));
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NotificationTemplate> activate(@PathVariable String id) {
        return ApiResponse.ok("Template activated", templateService.activate(id));
    }
}
