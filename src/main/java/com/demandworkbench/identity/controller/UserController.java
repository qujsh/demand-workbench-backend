package com.demandworkbench.identity.controller;

import com.demandworkbench.common.ApiResponse;
import com.demandworkbench.identity.application.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        return ApiResponse.success(new UserResponse(user.userNo(), user.nickname(), user.status(), "L0", "初访者"));
    }

    public record UserResponse(String userNo, String nickname, String status,
                               String publisherLevel, String publisherLevelName) { }
}
