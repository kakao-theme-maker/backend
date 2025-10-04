package com.komentum.global.exception;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;

public class CustomEntityNotFoundException extends EntityNotFoundException {
  public <E, I> CustomEntityNotFoundException(Class<E> entityClass, I id) {
    super(String.format("%s class doesn't have entity with id=%s", entityClass.getName(), id.toString()));
  }
}
