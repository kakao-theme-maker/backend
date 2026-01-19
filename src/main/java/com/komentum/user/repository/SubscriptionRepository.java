package com.komentum.user.repository;

import com.komentum.user.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  // 팔로워 수
  int countByUser_UserEmail(String userUserEmail);
  // userEmail: 구독 당한사람;  Subscription: 구독 한사람 수;

  //팔로잉 수
  int countBySubscriber_UserEmail(String subscriberUserEmail);
  // subscriber: 구독 한 사람;  Subscription: 구독 당한사람 수;

}