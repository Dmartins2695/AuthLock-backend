package com.webapp.pwmanager.appUser.requestsDto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenRefreshRequest {
  @NotBlank
  private String refreshToken;
  @NotBlank
  private String userName;
}
