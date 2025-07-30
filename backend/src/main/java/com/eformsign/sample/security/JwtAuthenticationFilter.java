package com.eformsign.sample.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("✅ JwtAuthenticationFilter invoked: " + request.getRequestURI());

        String uri = request.getRequestURI();
        if (uri.equals("/api/auth/login") || uri.equals("/api/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        System.out.println(">> 추출된 토큰: " + token);

        if (StringUtils.hasText(token) && jwtProvider.isTokenValid(token)) {
            String email = jwtProvider.getEmailFromToken(token);
            System.out.println(">> 토큰 이메일: " + email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            System.out.println(">> 인증 시도 전 SecurityContext: " + SecurityContextHolder.getContext().getAuthentication());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println(">> 인증 완료 후 SecurityContext: " + SecurityContextHolder.getContext().getAuthentication());
            System.out.println(">> SecurityContext 저장됨: " + SecurityContextHolder.getContext().getAuthentication());
        } else {
            System.out.println(">> 토큰이 없거나 유효하지 않음");
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); // "Bearer " 제거
        }
        return null;
    }
}