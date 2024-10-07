package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.demo.dto.ErrorResponseDto;
import com.example.demo.filter.CsrfCookieFilter;
import com.example.demo.filter.JwtTokenGenerationFilter;
import com.example.demo.filter.JwtTokenValidationFilter;
import com.example.demo.filter.RequestValidationBeforeFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;
import java.util.Collections;

@Configuration
public class ProjecSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Permit access to H2 console and disable CSRF protection for it
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/h2-console/**", "/notices", "/contact").permitAll()
                .requestMatchers(HttpMethod.POST, "/register", "/apiLogin").permitAll()
                // .requestMatchers("/myAccount").hasAuthority("VIEWACCOUNT")
                // .requestMatchers("/myBalance").hasAuthority("VIEWBALANCE")
                // .requestMatchers("/myCards").hasAuthority("VIEWCARDS")
                // .requestMatchers("/myLoans").hasAuthority("VIEWLOANS")
                .requestMatchers("/myAccount").hasRole("ADMIN")
                .requestMatchers("/myBalance").hasRole("ADMIN")
                .requestMatchers("/myCards").hasRole("USER")
                .requestMatchers("/myLoans").hasRole("USER")
                .anyRequest().authenticated() // All other requests must be authenticated
                
        ).exceptionHandling(handling -> handling
                        .accessDeniedHandler(new AccessDeniedHandler(){
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response,
                                    AccessDeniedException accessDeniedException) throws IOException, ServletException, JsonProcessingException, java.io.IOException {
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        ErrorResponseDto errorResponseDto = new ErrorResponseDto(request.getServletPath(),HttpStatus.FORBIDDEN ,accessDeniedException.getMessage(), new Date());
                                        // throw new ResponseStatusException(HttpStatusCode.valueOf(403), objectMapper.writeValueAsString(errorResponseDto));
                                        response.setContentType("application/json");
                                        response.setCharacterEncoding("UTF-8");
                                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                        response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
                            }
                        }));;

        http.cors(cors -> cors.configurationSource(new CorsConfigurationSource() {

            @Override
            @Nullable
            public CorsConfiguration getCorsConfiguration(HttpServletRequest arg0) {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(Collections.singletonList("*"));
                config.setAllowedMethods(Collections.singletonList("*"));
                config.setAllowedHeaders(Collections.singletonList("*"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                config.setExposedHeaders(Arrays.asList("Authorization"));
                return config;
            }

        }));

        CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler = new CsrfTokenRequestAttributeHandler();
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/contact", "/apiLogin") // Disable CSRF for H2 console
                .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new RequestValidationBeforeFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new JwtTokenValidationFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new JwtTokenGenerationFilter(), BasicAuthenticationFilter.class);

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers(headers -> headers
                .frameOptions().sameOrigin() // Allow H2 console to use frames
        );

        http.formLogin(Customizer.withDefaults()); // Enable form login

        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }
/*
   @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        UserDetails user1 = User.builder().username("user1").password("{noop}12345").roles("employee").build();
        UserDetails user2 = User.builder().username("user2").password("{noop}12345").roles("employee").build();
        return new InMemoryUserDetailsManager(user1, user2);
    }
*/
    // @Bean
    // public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource){
    // return new JdbcUserDetailsManager(dataSource);
    // }

    // @Bean
    // public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource){
    // System.out.println("Inside customw JDBC Userdetails
    // Manager................");
    // JdbcUserDetailsManager jdbcUserDetailsManager = new
    // JdbcUserDetailsManager(dataSource);
    // jdbcUserDetailsManager.setUsersByUsernameQuery("SELECT email,pwd,1 as enabled
    // FROM Customer WHERE email=?");
    // jdbcUserDetailsManager.setAuthoritiesByUsernameQuery("SELECT email,role FROM
    // Customer WHERE email=?");
    // return jdbcUserDetailsManager;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {

        AuthenticationProvider authenticationProvider = new EazyBankUserNamePwdAuthenticationProvider(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authenticationProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;

    }
}
