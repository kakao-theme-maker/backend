package com.komentum.user.domain;

import com.komentum.global.security.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"user\"")
@EntityListeners(AuditingEntityListener.class)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false, updatable = false)
  Long userId;
  @Column(unique = true, nullable = false, updatable = false)
  String publicUserId;
  @Column(unique = true, nullable = false)
  String userEmail;
  @Column
  String name;
  @Column
  Gender gender;
  @Getter(AccessLevel.NONE)
  @Column
  String encryptedPassword;
  @Column
  LocalDate birth;
  @Column
  String profileImgUrl;
  @Column
  String profileImgName;
  @Column
  String introduce;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(20) default 'USER'")
  UserRole role;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(20) default 'LOCAL'")
  @Builder.Default
  AuthProvider authProvider = AuthProvider.LOCAL;
  @CreatedDate
  LocalDateTime createdAt;
  @LastModifiedDate
  LocalDateTime updatedAt;

  public boolean matchPassword(String rawPassword, PasswordEncoder passwordEncoder) {
    return passwordEncoder.matches(rawPassword, this.encryptedPassword);
  }

  /**
   * 게스트 사용자 비영속 엔티티를 생성한다
   * */
  public static User createGuest(String publicUserId) {
    return User.builder()
        .publicUserId(publicUserId)
        .role(UserRole.GUEST)
        .userEmail(publicUserId + "@guest.com")
        .build();
  }

  /**
   * 게스트 사용자를 일반 사용자(USER)로 승격한다. 기존 ID(userId, publicUserId)는 유지된다.
   *
   * @throws IllegalStateException 현재 사용자가 GUEST 권한이 아닌 경우
   */
  public void promoteToUser(String userEmail, String encryptedPassword, String name) {
    if (this.role != UserRole.GUEST) {
      throw new IllegalStateException("[User Promotion] 게스트 사용자만 일반 사용자로 승격할 수 있습니다");
    }
    this.userEmail = userEmail;
    this.encryptedPassword = encryptedPassword;
    this.name = name;
    this.role = UserRole.USER;
  }

  /**
   * 게스트 사용자를 소셜 로그인 일반 사용자(USER)로 승격한다. 기존 ID(userId, publicUserId)는 유지된다.
   *
   * @throws IllegalStateException 현재 사용자가 GUEST 권한이 아닌 경우
   */
  public void promoteToSocialUser(String userEmail, String name, String profileImgUrl,
      AuthProvider authProvider) {
    if (this.role != UserRole.GUEST) {
      throw new IllegalStateException("[User Promotion] 게스트 사용자만 일반 사용자로 승격할 수 있습니다");
    }
    this.userEmail = userEmail;
    this.name = name;
    this.profileImgUrl = profileImgUrl;
    this.authProvider = authProvider;
    this.role = UserRole.USER;
  }
}
