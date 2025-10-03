package com.metamapa.userManager.authManager.services;

import com.metamapa.userManager.authManager.models.entities.User;
import com.metamapa.userManager.authManager.models.entities.dto.LoginDto;
import com.metamapa.userManager.authManager.models.entities.dto.NewUserDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserTokensDto;
import com.metamapa.userManager.authManager.models.repositories.UserRepository;
import java.util.Optional;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticacionService implements IAuthenticationService{
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  public AuthenticacionService(PasswordEncoder passwordEncoder, UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  @Override
  public UserTokensDto register(NewUserDto request) {
    User user = User.builder()
        .name(request.getName())
        .username(request.getUsername())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .build();
    //save user
    userRepository.save(user);
    var token = jwtService.generateAccessToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    UserTokensDto resp  = UserTokensDto.builder().
        accessToken(token).refreshToken(refreshToken).
        build();
    return resp;
  }

  @Override
  public UserTokensDto login(LoginDto request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getUsername(), request.getPassword()
        )
    );

    User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
        () -> new UsernameNotFoundException("Not found")
    );
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    return UserTokensDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  @Override
  public UserTokensDto refresh(String tokenHeader) {
    if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Token not valid");
    }

    String username = jwtService.extractUsername(tokenHeader.substring(7));
    if (username == null) {
      throw new UsernameNotFoundException("User Not found by token");
    }

    User user = userRepository.findByUsername(username)
        .orElseThrow(()-> new UsernameNotFoundException(username + " not found"));

    if (!jwtService.isTokenValid(tokenHeader.substring(7), user)) {
      throw new IllegalArgumentException("Token not valid, it's expired or incorrect");
    }

    return UserTokensDto.builder()
            .accessToken(jwtService.generateAccessToken(user))
            .refreshToken(tokenHeader.substring(7))
        .build();
  }
}
