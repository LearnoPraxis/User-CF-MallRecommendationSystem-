package com.mall.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf().disable()
            // 禁用Session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求授权
            .authorizeRequests()
            // 允许匿名访问的接口
            .antMatchers(
                "/api/auth/login",
                "/api/auth/register",
                "/api/product/list",
                "/api/product/detail/**",
                "/api/product/hot",
                "/api/product/new",
                "/api/category/list",
                "/api/category/tree",
                "/api/forum/list",
                "/api/forum/detail/**",
                "/api/recommend/**",
                "/api/comment/list/**",
                "/api/file/upload",
                "/uploads/**"
            ).permitAll()
            // Swagger文档
            .antMatchers(
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**"
            ).permitAll()
            // OPTIONS请求
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            // 管理员接口
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            // 其他请求需要认证
            .anyRequest().authenticated()
            .and()
            // 异常处理
            .exceptionHandling()
            // 未认证处理
            .authenticationEntryPoint((request, response, authException) -> {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.setStatus(401);
                response.getWriter().write(objectMapper.writeValueAsString(
                        Result.unauthorized("请先登录")
                ));
            })
            // 无权限处理
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.setStatus(403);
                response.getWriter().write(objectMapper.writeValueAsString(
                        Result.forbidden("没有权限访问该资源")
                ));
            });

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
