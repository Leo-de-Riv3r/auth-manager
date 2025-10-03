package com.metamapa.userManager.authManager.models.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class NewUserDto {
  private String username;
  private String name;
  private String password;
}
