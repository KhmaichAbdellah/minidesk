package com.minidesk.service;

import com.minidesk.dto.request.UpdateProfileRequest;
import com.minidesk.dto.request.UserRequest;
import com.minidesk.dto.response.UserResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    void toggleUserStatus(Long id);// active ou desactive un user
    UserResponse updateProfile(Long id, @Valid UpdateProfileRequest request);

}
