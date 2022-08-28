package com.webapp.pwmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TokenRefreshResponse {
  private List<String> roles;
}
