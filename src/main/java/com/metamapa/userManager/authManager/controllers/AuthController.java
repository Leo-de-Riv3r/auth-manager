package com.metamapa.userManager.authManager.controllers;

import com.metamapa.userManager.authManager.models.entities.dto.LoginDto;
import com.metamapa.userManager.authManager.models.entities.dto.NewUserDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserRolesAndAuthoritiesDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserTokensDto;
import com.metamapa.userManager.authManager.services.IAuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final IAuthenticationService authenticationService;

  public AuthController(IAuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/refresh")
  public ResponseEntity<UserTokensDto> refreshTokens(@RequestHeader(HttpHeaders.AUTHORIZATION) String tokenHeader) {
    return ResponseEntity.ok(authenticationService.refresh(tokenHeader));
  }

  @PostMapping("/register")
  public ResponseEntity<UserTokensDto> registerUser(@RequestBody NewUserDto request) {
    return ResponseEntity.ok(authenticationService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<UserTokensDto> logUser(@RequestBody LoginDto request) {
    UserTokensDto resp = authenticationService.login(request);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/user/roles-permisos")
  public ResponseEntity<UserRolesAndAuthoritiesDto> getRolesPermisos(@RequestHeader(HttpHeaders.AUTHORIZATION) String reqToken) {
    return ResponseEntity.ok(authenticationService.getRolesAndAuthorities(reqToken));
  }
}
