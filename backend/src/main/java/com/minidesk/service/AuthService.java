package com.minidesk.service;

import com.minidesk.dto.request.LoginRequest;
import com.minidesk.dto.response.AuthResponse;

public interface AuthService {
    //auth et retourne un jwt
    AuthResponse login(LoginRequest request);
}
