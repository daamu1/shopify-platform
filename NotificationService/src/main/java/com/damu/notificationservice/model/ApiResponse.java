package com.damu.notificationservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {
    private final boolean success;
    private final int statusCode;
    private final String message;
    private final T data;
    private final List<ApiError> errors;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    @JsonCreator
    private ApiResponse(boolean success,
                        int statusCode,
                        String message,
                        T data,
                        List<ApiError> errors,
                        LocalDateTime timestamp) {
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        this.errors = (errors == null || errors.isEmpty()) ? null : List.copyOf(errors);
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
    }

    private ApiResponse(boolean success, int statusCode, String message, T data, List<ApiError> errors) {
        this(success, statusCode, message, data, errors, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, 200, message, data, null);
    }

    public static <T> ApiResponse<T> ok(String message, int statusCode, T data) {
        return new ApiResponse<>(true, statusCode, message, data, null);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, 200, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String message, int statusCode) {
        return new ApiResponse<>(false, statusCode, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String message, int statusCode, List<ApiError> errors) {
        return new ApiResponse<>(false, statusCode, message, null, errors);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class ApiError {
        private final String field;
        private final String reason;

        @JsonCreator
        private ApiError(String field, String reason) {
            this.field = field;
            this.reason = reason;
        }

        public static ApiError of(String field, String reason) {
            return new ApiError(field, reason);
        }

        public static ApiError of(String reason) {
            return new ApiError(null, reason);
        }
    }
}
