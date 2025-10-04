package com.metamapa.userManager.authManager.config;

import com.metamapa.userManager.authManager.models.entities.User;
import com.metamapa.userManager.authManager.models.repositories.UserRepository;
import com.metamapa.userManager.authManager.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  //private final TokenRepository;
  private final UserRepository userRepository;
  
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    String path = request.getServletPath();

    // 🔹 Solo analizar el token si la ruta es /auth/login o /auth/refresh


    if (request.getServletPath().equals("/auth/login") ||
        request.getServletPath().equals("/auth/register")) {
      filterChain.doFilter(request, response);
      return;
    }

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    String username = jwtService.extractUsername(token);
    if (username == null || SecurityContextHolder.getContext().getAuthentication() != null){
      filterChain.doFilter(request, response);
      return;
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    Optional<User> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!jwtService.isTokenValid(token, user.get())) {
      filterChain.doFilter(request, response);
      return;
    }

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
        null,
        userDetails.getAuthorities());

    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    filterChain.doFilter(request, response);
  }
}
