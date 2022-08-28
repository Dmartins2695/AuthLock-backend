package com.webapp.pwmanager.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AuthenticationRequest {

    @SerializedName("remember-me")
    String rememberMe;
    private String userName;
    private String password;

}
