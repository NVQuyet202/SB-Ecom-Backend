package com.ecommerce.sb_ecom.security;

import com.ecommerce.sb_ecom.model.AppRole;
import com.ecommerce.sb_ecom.model.Role;
import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.repository.RoleRepository;
import com.ecommerce.sb_ecom.repository.UserRepository;
import com.ecommerce.sb_ecom.security.jwt.AuthEntryPointJwt;
import com.ecommerce.sb_ecom.security.jwt.AuthTokenFilter;
import com.ecommerce.sb_ecom.security.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Set;

@Configuration
@EnableWebSecurity // Bật Spring Security, nếu bỏ annotation này, toàn bộ config k hoạt động
@EnableMethodSecurity // Thêm để dùng @PreAuthorize chi tiết hơn ở controller
public class WebSecurityConfig {

    //Đây là service Spring dùng lấy user từ db, Spring security sẽ gọi
    @Autowired
    UserDetailServiceImpl userDetailService;

    //Class xử lý lỗi nếu k gửi jwt, jwt sai, jwt expired 401
    @Autowired
    private AuthEntryPointJwt unAuthorizeHandler;

    //Tạo JWT filter, chịu trách nhiệm lấy token -> validate token -> load user -> Tạo Authentication -> Set SecurityContext
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    //Đây là thành phần xử lý Login bằng username/password.
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        //Nói vs Spring: tìm user thì gọi userDetailService
        authenticationProvider.setUserDetailsService(userDetailService);
        //Nói vs Spring: Password trong DB đang mã hóa bằng BCrypt, Spring sẽ dùng BCrypt để kiểm tra.
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    Nó quy định:
        API nào được phép
        API nào phải login
        Filter nào chạy
        Session hoạt động thế nào
    */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()) // thêm dòng này, xem CorsConfig bên dưới
                //Ý nghĩa: Không tạo session, không lưu User trên server, mỗi request phải gửi jwt
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unAuthorizeHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/h2-console/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()   // chỉ GET mới public
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/seller/**").hasAnyRole("SELLER", "ADMIN")
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers(
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"
        ));
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Retrieve or create roles
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> {
                        Role newUserRole = new Role(AppRole.ROLE_USER);
                        return roleRepository.save(newUserRole);
                    });

            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                    .orElseGet(() -> {
                        Role newSellerRole = new Role(AppRole.ROLE_SELLER);
                        return roleRepository.save(newSellerRole);
                    });

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role newAdminRole = new Role(AppRole.ROLE_ADMIN);
                        return roleRepository.save(newAdminRole);
                    });

            Set<Role> userRoles = Set.of(userRole);
            Set<Role> sellerRoles = Set.of(sellerRole);
            Set<Role> adminRoles = Set.of(userRole, sellerRole, adminRole);


            // Create users if not already present
            if (!userRepository.existsByUserName("user1")) {
                User user1 = User.builder()
                        .userName("user1")
                        .email("user1@example.com")
                        .password(passwordEncoder.encode("password1"))
                        .build();
                userRepository.save(user1);
            }

            if (!userRepository.existsByUserName("seller1")) {
                User seller1 = User.builder()
                        .userName("seller1")
                        .email("seller1@example.com")
                        .password(passwordEncoder.encode("password2"))
                        .build();
                userRepository.save(seller1);
            }

            if (!userRepository.existsByUserName("admin")) {
                User admin = User.builder()
                        .userName("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("adminPass"))
                        .build();
                userRepository.save(admin);
            }

            // Update roles for existing users
            userRepository.findByUserName("user1").ifPresent(user -> {
                user.setRoles(userRoles);
                userRepository.save(user);
            });

            userRepository.findByUserName("seller1").ifPresent(seller -> {
                seller.setRoles(sellerRoles);
                userRepository.save(seller);
            });

            userRepository.findByUserName("admin").ifPresent(admin -> {
                admin.setRoles(adminRoles);
                userRepository.save(admin);
            });
        };
    }

}
