package com.komentum.catalog.dto;

public enum ComponentType {
  THEME,
  DESIGN;

  public static ComponentType fromString(String type) {
    for (ComponentType componentType : ComponentType.values()) {
      if (componentType.name().equalsIgnoreCase(type)) {
        return componentType;
      }
    }
    return null;
  }
}
