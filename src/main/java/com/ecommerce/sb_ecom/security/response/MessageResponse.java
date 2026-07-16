package com.ecommerce.sb_ecom.security.response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
        private HttpStatus status;
        private String message;
}
