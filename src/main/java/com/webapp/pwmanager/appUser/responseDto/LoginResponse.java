package com.webapp.pwmanager.appUser.responseDto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class LoginResponse {
	
	private String token;
	private List roles;
	
	

}
