package com.theme.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private User subscriber;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @CreatedDate
    private LocalDateTime subscribedAt;
}