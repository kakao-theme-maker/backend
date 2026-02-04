package com.komentum.user.repository;

import com.komentum.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByUserEmail(String userEmail);

  Optional<User> findByPublicUserId(String publicUserId);

}
