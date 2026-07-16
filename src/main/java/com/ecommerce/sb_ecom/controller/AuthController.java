package com.ecommerce.sb_ecom.controller;

import com.ecommerce.sb_ecom.model.AppRole;
import com.ecommerce.sb_ecom.model.Role;
import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.repository.RoleRepository;
import com.ecommerce.sb_ecom.repository.UserRepository;
import com.ecommerce.sb_ecom.security.jwt.JwtUtils;
import com.ecommerce.sb_ecom.security.request.LoginRequest;
import com.ecommerce.sb_ecom.security.request.SignUpRequest;
import com.ecommerce.sb_ecom.security.response.MessageResponse;
import com.ecommerce.sb_ecom.security.response.UserInfoResponse;
import com.ecommerce.sb_ecom.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/signIn")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message","Bad credentials");
            map.put("status", HttpStatus.UNAUTHORIZED);
            return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response =  UserInfoResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        return new ResponseEntity<>(
                response,
                headers,
                HttpStatus.OK);
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUserName(signUpRequest.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(HttpStatus.BAD_REQUEST,"Username already exists"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse(HttpStatus.BAD_REQUEST,"Email is already in use!"));
        }

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {

            Role userRole = roleRepository
                    .findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() ->
                            new RuntimeException("Role is not found"));

            roles.add(userRole);

        } else {

            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository
                                .findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() ->
                                        new RuntimeException("Role is not found"));

                        roles.add(adminRole);
                        break;

                    case "seller":
                        Role sellerRole = roleRepository
                                .findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() ->
                                        new RuntimeException("Role is not found"));

                        roles.add(sellerRole);
                        break;

                    default:

                        Role userRole = roleRepository
                                .findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() ->
                                        new RuntimeException("Role is not found"));

                        roles.add(userRole);
                }
            });
        }

        User newUser = User.builder()
                .userName(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .roles(roles)
                .build();
        userRepository.save(newUser);
        return  ResponseEntity.ok(new MessageResponse(HttpStatus.OK,"User registered successfully!"));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        UserInfoResponse response = UserInfoResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
            return ResponseEntity.ok().body(response);

    }

    @PostMapping("/signOut")
    public ResponseEntity<?> signOutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse(HttpStatus.OK,"You've been signed out!"));
    }

}
