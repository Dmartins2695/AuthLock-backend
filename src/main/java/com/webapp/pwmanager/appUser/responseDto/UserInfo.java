package com.webapp.pwmanager.appUser.responseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {
	
	private String firstName;
	private String lastName;
	private String email;
	private Object roles;

}
