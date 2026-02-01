package com.komentum.user.repository;

import com.komentum.user.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  int countByUser_publicUserId(String userPublicUserId);
  // userEmail: 구독 당한사람;  Subscription: 구독 한사람 수;

  int countBySubscriber_publicUserId(String subscriberPublicUserId);
  // subscriber: 구독 한 사람;  Subscription: 구독 당한사람 수;

}