package com.example.demo.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.constants.ApplicationConstants;
import com.example.demo.dto.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("JwtTokenValidationFilter isssss caaaaaaaaaaaaaaaaaaaaaaaalllllllllleddddddddddddddd");
        String jwt = request.getHeader(ApplicationConstants.JWT_HEADER);
        if (jwt != null && !jwt.substring(0, 5).equalsIgnoreCase("Basic")) {
            Environment env = getEnvironment();
            try {
                if (env != null) {
                    String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                            ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    if (secretKey != null) {
                        Claims claims = Jwts
                                .parser()
                                .verifyWith(secretKey)
                                .build()
                                .parseSignedClaims(jwt)
                                .getPayload();
                        System.out.println("Claims are - -----------------------------" + claims.toString());
                        String username = claims.get("username").toString();
                        String authorities = claims.get("authorities").toString();
                        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
                                AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("Stored authenticationnnnnnnnnnnnnnnnnnnn is ssssssssssssssssssssss"
                                + authentication.toString());
                    }
                }
            } catch (Exception ex) {
                System.out.println(
                        "EXCEPPPPPPPTIOOOOOOOOOOOOOOOOOOOONNNNNNNNNNNNNNNNN     CCCCCCCAAAAAAAATTTTTCCCCCHHHHHHHHHEEEDDDDDDD");
                System.out.println("Exception thrown is - " + ex.getClass().getName());
                ErrorResponseDto errorResponseDto = new ErrorResponseDto(request.getServletPath(),
                        HttpStatus.BAD_REQUEST, ex.getMessage(), new Date());
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                ObjectMapper objectMapper = new ObjectMapper();

                String errorResponseDtoJSON = objectMapper.writeValueAsString(errorResponseDto);
                response.getWriter().write(errorResponseDtoJSON);
                response.getWriter().flush();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().equals("/user");
    }

}
