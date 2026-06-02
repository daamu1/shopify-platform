package com.damu.UserService.service;

import com.damu.UserService.model.UserProfileResponse;
import com.damu.UserService.model.UserRegistrationRequest;

public interface UserService {
    UserProfileResponse registerOrGetUser(UserRegistrationRequest request);
    UserProfileResponse getUserByAuthSubject(String authSubject);
}
