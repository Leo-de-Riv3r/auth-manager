package com.metamapa.userManager.authManager.models.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class NewUserDto {
  private String username;
  private String password;
}
