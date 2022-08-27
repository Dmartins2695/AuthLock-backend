package com.webapp.pwmanager.appUser.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshResponse {
  private String accessToken;
  private String refreshToken;
}
