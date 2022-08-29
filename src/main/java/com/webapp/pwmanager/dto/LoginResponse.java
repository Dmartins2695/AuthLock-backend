package com.webapp.pwmanager.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class LoginResponse {
	private String accessToken;
	private List<String> roles;

}
