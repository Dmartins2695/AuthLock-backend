package com.webapp.pwmanager.security;

import com.webapp.pwmanager.service.AppUserService;
import com.webapp.pwmanager.jwt.JWTAuthenticationFilter;
import com.webapp.pwmanager.util.JWTTokenHelper;
import com.webapp.pwmanager.util.SecurityCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;

import static com.webapp.pwmanager.domain.AppUserRole.ADMIN;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfiguration{

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private JWTTokenHelper jWTTokenHelper;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private final SecurityCipher securityCipher;

    @Autowired
    public SecurityConfiguration(AppUserService appUserService, PasswordEncoder passwordEncoder, SecurityCipher securityCipher) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
        this.securityCipher = securityCipher;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/api/v*/auth/login", "/api/v*/registration/**","/api/v*/auth/refresh-token");
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .requiresChannel().anyRequest().requiresSecure()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .authorizeRequests((request) ->
                        request.antMatchers( "/api/v*/auth/login", "/api/v*/registration/**","/api/v*/auth/refresh-token").permitAll()
                                .antMatchers("/api/v*/password/**").hasRole(ADMIN.name())
                                .antMatchers("/api/v*/user/**").hasRole(ADMIN.name())
                                .anyRequest().authenticated()
                );
        http.addFilterBefore(new JWTAuthenticationFilter(appUserService, jWTTokenHelper,securityCipher),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(appUserService);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}