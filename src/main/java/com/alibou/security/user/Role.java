package com.alibou.security.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alibou.security.user.Permission.ADMIN_CREATE;
import static com.alibou.security.user.Permission.ADMIN_DELETE;
import static com.alibou.security.user.Permission.ADMIN_READ;
import static com.alibou.security.user.Permission.ADMIN_UPDATE;
import static com.alibou.security.user.Permission.EDITOR_CREATE;
import static com.alibou.security.user.Permission.EDITOR_DELETE;
import static com.alibou.security.user.Permission.EDITOR_READ;
import static com.alibou.security.user.Permission.EDITOR_UPDATE;

@RequiredArgsConstructor
public enum Role {

  USER(Collections.emptySet()),
  EDITOR(
          Set.of(
                  EDITOR_READ,
                  EDITOR_UPDATE,
                  EDITOR_DELETE,
                  EDITOR_CREATE
          )
  ),
  ADMIN(
          Set.of(
                  // Admin has FULL permissions - both admin and editor
                  ADMIN_READ,
                  ADMIN_UPDATE,
                  ADMIN_DELETE,
                  ADMIN_CREATE,
                  EDITOR_READ,
                  EDITOR_UPDATE,
                  EDITOR_DELETE,
                  EDITOR_CREATE
          )
  );

  @Getter
  private final Set<Permission> permissions;

  public List<SimpleGrantedAuthority> getAuthorities() {
    var authorities = getPermissions()
            .stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
            .collect(Collectors.toList());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    return authorities;
  }
}
