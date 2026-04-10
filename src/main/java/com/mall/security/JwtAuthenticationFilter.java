package com.mall.security;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(TOKEN_HEADER);

        if (StrUtil.isNotBlank(header) && header.startsWith(TOKEN_PREFIX)) {
            String token = header.substring(TOKEN_PREFIX.length());

            if (jwtTokenUtil.validateToken(token)) {
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                String username = jwtTokenUtil.getUsernameFromToken(token);
                Integer role = jwtTokenUtil.getRoleFromToken(token);

                // 设置权限
                String authority = role == 1 ? "ROLE_ADMIN" : "ROLE_USER";
                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority(authority)
                );

                // 创建认证信息
                LoginUser loginUser = new LoginUser(userId, username, role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
