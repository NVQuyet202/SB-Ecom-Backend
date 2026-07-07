package com.ecommerce.sb_ecom.security;

import com.ecommerce.sb_ecom.security.jwt.AuthEntryPointJwt;
import com.ecommerce.sb_ecom.security.jwt.AuthTokenFilter;
import com.ecommerce.sb_ecom.security.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Bật Spring Security, nếu bỏ annotation này, toàn bộ config k hoạt động
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
                .exceptionHandling(ex ->ex.authenticationEntryPoint(unAuthorizeHandler))
                //Ý nghĩa: Không tạo session, không lưu User trên server, mỗi request phải gửi jwt
                .sessionManagement(
                        session -> session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests.requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);
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
}
