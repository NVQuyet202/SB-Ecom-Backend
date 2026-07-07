package com.ecommerce.sb_ecom.security.jwt;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;

}