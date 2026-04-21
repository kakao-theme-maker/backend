package com.komentum.global.security;

public enum UserRole {
  GUEST,
  USER,
  ADMIN;

  public String getAuthority() {
    return "ROLE_" + name();
  }
}
