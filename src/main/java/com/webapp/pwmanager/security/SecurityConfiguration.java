package com.webapp.pwmanager.security;

import com.webapp.pwmanager.appUser.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    /**
     * Prod Configuration with https
     */
   /* @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel((channel) -> channel.anyRequest().requiresSecure())
            .formLogin()
            .and()
            .authorizeRequests()
            .anyRequest()
            .authenticated();

        return http.build();
    }*/

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfiguration(PasswordEncoder passwordEncoder,
                                 AppUserService appUserService) {
        this.passwordEncoder = passwordEncoder;
        this.appUserService = appUserService;
    }


    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(appUserService).passwordEncoder(passwordEncoder);

        // Get AuthenticationManager
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/v*/registration/**")
                .permitAll()
                .and()
                .formLogin()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated().and()
                .authenticationManager(authenticationManager);

        return http.build();
    }

}
