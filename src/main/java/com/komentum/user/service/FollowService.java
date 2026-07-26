package com.komentum.user.service;

import com.komentum.user.domain.Follow;
import com.komentum.user.domain.User;
import com.komentum.user.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final UserEntityFinder userEntityFinder;

  public void follow(String followerPublicUserId, String followeePublicUserId) {
    User follower = userEntityFinder.findUserEntity(followerPublicUserId);
    User followee = userEntityFinder.findUserEntity(followeePublicUserId);

    if (follower.getUserId().equals(followee.getUserId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Users cannot follow themselves");
    }
    if (followRepository.existsByFollowerAndFollowee(follower, followee)) {
      return;
    }

    try {
      followRepository.saveAndFlush(Follow.createTransient(follower, followee));
    } catch (DataIntegrityViolationException e) {
      if (!followRepository.existsByFollowerAndFollowee(follower, followee)) {
        throw e;
      }
    }
  }

  @Transactional
  public void unfollow(String followerPublicUserId, String followeePublicUserId) {
    User follower = userEntityFinder.findUserEntity(followerPublicUserId);
    User followee = userEntityFinder.findUserEntity(followeePublicUserId);
    followRepository.deleteByFollowerAndFollowee(follower, followee);
  }
}
