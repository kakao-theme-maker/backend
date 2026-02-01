package com.komentum.user.service;

import com.komentum.user.domain.User;

public interface UserEntityFinder {
  User findUserEntity(String publicUserId);


}
