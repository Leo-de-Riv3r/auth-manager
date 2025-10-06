package com.metamapa.userManager.authManager.services;

import com.metamapa.userManager.authManager.models.entities.Permiso;
import com.metamapa.userManager.authManager.models.entities.Rol;
import com.metamapa.userManager.authManager.models.entities.TipoRol;
import com.metamapa.userManager.authManager.models.entities.User;
import com.metamapa.userManager.authManager.models.entities.dto.LoginDto;
import com.metamapa.userManager.authManager.models.entities.dto.NewUserDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserRolesAndAuthoritiesDto;
import com.metamapa.userManager.authManager.models.entities.dto.UserTokensDto;
import com.metamapa.userManager.authManager.models.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticacionService implements IAuthenticationService{
  //logica para creacion de administrador
  @Value("${privateAdminName}")
  private String secretAdminName;
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
    Optional<User> userInDb = userRepository.findByUsername(request.getUsername());
    if (userInDb.isPresent()) {
      throw new EntityExistsException("User with same username already exists");
    }
    User user = User.builder()
        .username(request.getUsername())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .build();
    //save user
    //controlo si el usuario tiene name master of puppets
    Rol rol = new Rol();
    if (Objects.equals(request.getUsername(), secretAdminName)) {
      //crear admin
      rol.setTiporol(TipoRol.ADMINISTRADOR);
      rol.setPermisos(List.of(Permiso.GESTION_COLECCIONES, Permiso.EDITAR_HECHO, Permiso.GESTIONAR_HECHOS, Permiso.GESTIONAR_SOLICITUDES));
    } else {
    rol.setTiporol(TipoRol.CONTRIBUYENTE);
    rol.setPermisos(List.of(Permiso.EDITAR_HECHO));
    }
    user.setRol(rol);
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
    User user = getUserByToken(tokenHeader);

    if (!jwtService.isTokenValid(tokenHeader.substring(7), user)) {
      throw new IllegalArgumentException("Token not valid, it's expired or incorrect");
    }

    return UserTokensDto.builder()
            .accessToken(jwtService.generateAccessToken(user))
            .refreshToken(tokenHeader.substring(7))
        .build();
  }

  @Override
  public UserRolesAndAuthoritiesDto getRolesAndAuthorities(String reqToken) {
    User user = getUserByToken(reqToken);

    if (!jwtService.isTokenValid(reqToken.substring(7), user)) {
      throw new IllegalArgumentException("Token not valid, it's expired or incorrect");
    }

    return UserRolesAndAuthoritiesDto.builder()
        .username(user.getUsername())
        .rol(user.getRol().getTiporol())
        .permisos(user.getRol().getPermisos())
        .build();
  }

  public User getUserByToken(String tokenHeader) {
    if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Token not valid");
    }

    String username = jwtService.extractUsername(tokenHeader.substring(7));
    if (username == null) {
      throw new UsernameNotFoundException("User Not found by token");
    }

    User user = userRepository.findByUsername(username)
        .orElseThrow(()-> new UsernameNotFoundException(username + " not found"));
    return user;
  }
}
