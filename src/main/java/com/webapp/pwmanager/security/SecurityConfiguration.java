package com.webapp.pwmanager.security;

import com.webapp.pwmanager.appUser.service.AppUserService;
import com.webapp.pwmanager.security.jwt.JWTAuthenticationFilter;
import com.webapp.pwmanager.security.jwt.JWTTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.concurrent.TimeUnit;

import static com.webapp.pwmanager.appUser.domain.AppUserRole.ADMIN;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private JWTTokenHelper jWTTokenHelper;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfiguration(AppUserService appUserService, PasswordEncoder passwordEncoder) {
        this.appUserService = appUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable().cors().and().headers().frameOptions().disable().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .authorizeRequests((request) ->
                        request.antMatchers( "/api/v*/auth/login", "/api/v*/registration/**").permitAll()
                                .antMatchers("/api/v*/password").hasAuthority(ADMIN.name())
                                .antMatchers("/api/v*/user").hasAuthority(ADMIN.name())
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JWTAuthenticationFilter(appUserService, jWTTokenHelper),
                        UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
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
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}