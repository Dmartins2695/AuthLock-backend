package com.webapp.pwmanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponse {
	
	private String firstName;
	private String lastName;
	private String email;
	private Object roles;

}
