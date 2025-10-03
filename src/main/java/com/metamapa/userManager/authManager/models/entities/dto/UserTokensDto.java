package com.metamapa.userManager.authManager.models.entities.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTokensDto {
  private String accessToken;
  private String refreshToken;
}
