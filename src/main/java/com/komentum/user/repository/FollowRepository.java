package com.komentum.user.repository;

import com.komentum.user.domain.Follow;
import com.komentum.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  boolean existsByFollowerAndFollowee(User follower, User followee);

  long deleteByFollowerAndFollowee(User follower, User followee);

  int countByFollowee_PublicUserId(String publicUserId);

  int countByFollower_PublicUserId(String publicUserId);
}
