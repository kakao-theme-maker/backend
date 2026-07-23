package com.komentum.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.komentum.global.utils.FileManager;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.repository.SubscriptionRepository;
import com.komentum.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private PostService postService;

  @Mock
  private BoardManagementHelper boardManagementHelper;

  @Mock
  private FileManager fileManager;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("이메일로 사용자를 조회한다")
  void findUserEntityByEmail_success() {
    String userEmail = "user@example.com";
    User user = new User();
    given(userRepository.findByUserEmail(userEmail)).willReturn(Optional.of(user));

    User result = userService.findUserEntityByEmail(userEmail);

    assertThat(result).isSameAs(user);
  }

  @Test
  @DisplayName("이메일에 해당하는 사용자가 없으면 예외를 던진다")
  void findUserEntityByEmail_notFound() {
    String userEmail = "missing@example.com";
    given(userRepository.findByUserEmail(userEmail)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findUserEntityByEmail(userEmail))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("user not found");
  }
}
