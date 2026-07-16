package com.ecommerce.sb_ecom.security.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

}