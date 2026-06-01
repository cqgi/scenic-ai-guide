package com.scenic.ai.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashTests {

    @Test
    void seededAdminPasswordMatchesDocumentedPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches(
                "admin123",
                "$2a$10$6EmNcmMjpi541ZLD2H8OD.sIf3xyATI2Q2v3CZIdMktxf7/HUJc6m"
        )).isTrue();
    }
}
