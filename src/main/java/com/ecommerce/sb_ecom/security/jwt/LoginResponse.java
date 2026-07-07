package com.ecommerce.sb_ecom.security.jwt;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginResponse {
    private String jwtToken;
    private String username;
    private List<String> roles;

}
