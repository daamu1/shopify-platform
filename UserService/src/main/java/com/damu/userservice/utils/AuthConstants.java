package com.damu.userservice.utils;

import java.util.regex.Pattern;

public final class AuthConstants {

    public static final String DEFAULT_ROLE = "CUSTOMER";
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final long LOCK_MINUTES = 15;
    public static final long PASSWORD_RESET_TOKEN_MINUTES = 30;
    public static final long EMAIL_VERIFICATION_TOKEN_HOURS = 24;
    public static final String USER_REGISTERED_EVENT = "user_registered";
    public static final String USER_LOGGED_IN_EVENT = "user_logged_in";
    public static final String PASSWORD_RESET_REQUESTED_EVENT = "password_reset_requested";
    public static final String PASSWORD_RESET_COMPLETED_EVENT = "password_reset_completed";
    public static final String PASSWORD_CHANGED_EVENT = "password_changed";
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private AuthConstants() {
    }
}
