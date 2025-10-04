package com.metamapa.userManager.authManager.services;

import com.metamapa.userManager.authManager.models.entities.dto.LoginDto;
import com.metamapa.userManager.authManager.models.entities.dto.NewUserDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserRolesAndAuthoritiesDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserTokensDto;

public interface IAuthenticationService {
  UserTokensDto register(NewUserDto request);

  UserTokensDto login(LoginDto request);

  UserTokensDto refresh(String tokenHeader);

  UserRolesAndAuthoritiesDto getRolesAndAuthorities(String reqToken);
}
