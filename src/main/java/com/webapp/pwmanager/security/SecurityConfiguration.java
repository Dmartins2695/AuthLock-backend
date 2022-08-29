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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.webapp.pwmanager.domain.AppUserRole.ADMIN;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

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
                        request.antMatchers( "/api/v*/auth/login", "/api/v*/registration/**","/api/v*/auth/refresh-token").permitAll()
                                .antMatchers("/api/v*/password/**").hasRole(ADMIN.name())
                                .antMatchers("/api/v*/user/**").hasRole(ADMIN.name())
                                .anyRequest().authenticated()

                )
                .addFilterBefore(new JWTAuthenticationFilter(appUserService, jWTTokenHelper,securityCipher),
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