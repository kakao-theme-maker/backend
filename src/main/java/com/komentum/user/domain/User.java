package com.komentum.user.domain;

import com.komentum.global.security.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"user\"")
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @Column(unique = true, nullable = false)
  String userEmail;
  @Column
  Gender gender;
  @Column
  LocalDate birth;
  @Column
  String profileImg;
  @Column
  String introduce;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(20) default 'USER'")
  UserRole role;
  @CreatedDate
  LocalDateTime createdAt;
  @LastModifiedDate
  LocalDateTime updatedAt;
}
