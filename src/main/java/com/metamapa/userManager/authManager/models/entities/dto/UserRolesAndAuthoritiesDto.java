package com.metamapa.userManager.authManager.models.entities.dto;

import com.metamapa.userManager.authManager.models.entities.Permiso;
import com.metamapa.userManager.authManager.models.entities.TipoRol;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRolesAndAuthoritiesDto {
  private String username;
  private TipoRol rol;
  private List<Permiso> permisos;
}
