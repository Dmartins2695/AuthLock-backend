package com.webapp.pwmanager.jwt;

import com.webapp.pwmanager.util.JWTTokenHelper;
import com.webapp.pwmanager.util.SecurityCipher;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthenticationFilter extends OncePerRequestFilter {
	
	private final UserDetailsService userDetailsService;
	private final JWTTokenHelper jwtTokenHelper;
	@Autowired
	private final SecurityCipher securityCipher;
	
	public JWTAuthenticationFilter(UserDetailsService userDetailsService,JWTTokenHelper jwtTokenHelper,SecurityCipher securityCipher) {
		this.userDetailsService=userDetailsService;
		this.jwtTokenHelper=jwtTokenHelper;
		this.securityCipher=securityCipher;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		
		String authToken=jwtTokenHelper.getToken(request);
		
		if(null!=authToken) {
			
			String userName=jwtTokenHelper.getUsernameFromToken(authToken);
			
			if(null!=userName) {
				
				UserDetails userDetails=userDetailsService.loadUserByUsername(userName);
				
				if(jwtTokenHelper.validateToken(authToken, userDetails)) {
					
					UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetails(request));
					
						SecurityContextHolder.getContext().setAuthentication(authentication);

				}
				
			}
			
		}
		
		filterChain.doFilter(request, response);
		
		
		
	}

}
