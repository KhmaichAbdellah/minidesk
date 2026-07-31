package com.minidesk.dto.response;

import com.minidesk.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private long id;
    private String name;
    private String email;
    private Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;


}
