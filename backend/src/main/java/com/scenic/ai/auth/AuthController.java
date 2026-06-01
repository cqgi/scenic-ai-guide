package com.scenic.ai.auth;

import com.scenic.ai.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AdminUser user = adminUserRepository.findByUsername(request.username())
                .filter(AdminUser::isEnabled)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        return ApiResponse.ok(new LoginResponse(jwtService.createToken(user), AdminUserDto.from(user)));
    }

    @GetMapping("/me")
    public ApiResponse<AdminUserDto> me(@AuthenticationPrincipal AdminUser user) {
        return ApiResponse.ok(AdminUserDto.from(user));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String token, AdminUserDto user) {
    }

    public record AdminUserDto(Long id, String username, String displayName, String role) {
        static AdminUserDto from(AdminUser user) {
            return new AdminUserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
        }
    }
}
