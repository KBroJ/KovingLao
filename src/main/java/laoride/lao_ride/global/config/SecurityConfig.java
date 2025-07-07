package laoride.lao_ride.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    // 1. "/admin/**" 경로는 "ADMIN" 또는 "SUPER_ADMIN" 역할을 가진 사용자만 접근 가능
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER", "STAFF")
                    // 2. 그 외 모든 경로는 누구나 접근 허용
                    .anyRequest().permitAll()
            )
            .formLogin(form -> form
                    // 3. 커스텀 로그인 페이지 경로 설정
                    .loginPage("/admin/login")
                    // 4. 로그인 폼의 action URL. Spring Security가 이 요청을 가로채서 인증 처리
                    .loginProcessingUrl("/admin/login")
                    // 5. 로그인 성공 시 이동할 기본 경로
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .permitAll()
            )
            .logout(logout -> logout
                    // 6. 로그아웃 URL 설정
                    .logoutUrl("/admin/logout")
                    // 7. 로그아웃 성공 시 이동할 경로
                    .logoutSuccessUrl("/admin/login?logout")
                    .permitAll()
            );

        return http.build();
    }

}
