package org.example.analyticsservice.dto;

import java.util.List;

public record UserValidationResponse(
        Boolean valid,
        String username,
        List<String> roles
) {
}
