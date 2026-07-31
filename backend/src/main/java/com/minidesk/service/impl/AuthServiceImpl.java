package com.minidesk.service.impl;

import lombok.RequiredArgsConstructor;
import com.minidesk.dto.request.LoginRequest;
import com.minidesk.dto.response.AuthResponse;
import com.minidesk.entity.User;
import com.minidesk.exception.LoginException;
import com.minidesk.repository.UserRepository;
import com.minidesk.security.JwtService;
import com.minidesk.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;




    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new LoginException("utilisateur n'existe pas"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new LoginException("mot de passe incorrect");
        }
        //genere le token
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }
}
