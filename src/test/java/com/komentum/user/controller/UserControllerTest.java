package com.komentum.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.global.dto.CustomResponse;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.Follow;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserBirthUpdateDto;
import com.komentum.user.dto.UserGenderUpdateDto;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.FollowRepository;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerTest {

  String email = "admin1@gmail.com";
  String password = "qwer123!";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserDataGenerator userDataGenerator;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FollowRepository followRepository;
  @Autowired
  private PostRepository postRepository;
  @MockitoBean
  private FileManager fileManager;

  User user;
  @Autowired
  private MockMvcUtils mockMvcUtils;

  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    userDataGenerator.generateRetrieveTestUser(email, password);
    addPostForUser(email);
    user = userRepository.findByUserEmail(email).orElseThrow();
  }

  @AfterEach
  void tearDown() {
    userDataGenerator.deleteAllUsers();
  }

  // upload 확인용
  private void addPostForUser(String email) {
    User user = userRepository.findByUserEmail(email)
        .orElseThrow();
    Post post = Post.builder()
        .title("test")
        .content("test-content")
        .user(user)
        .postType(PostType.THEME_BOARD)
        .build();

    postRepository.save(post);
  }

  // Map 형태의 파라미터를 MultiValueMap으로 변환
  private LinkedMultiValueMap<String, String> params(Map<String, String> map) {
    LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.setAll(map);
    return params;
  }

  private UserResponseDto updateProfile(Map<String, ?> body) throws Exception {
    return mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<Map<String, ?>, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me")
            .httpMethod(HttpMethod.PATCH)
            .body(body)
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
  }

  @Test
  @DisplayName("사용자 조회 시 사용자 정보와 팔로워·팔로잉 수를 올바른 방향으로 반환한다")
  void retrieveUser_returnsUserInfoAndCorrectFollowCounts() throws Exception {
    //given
    user.setIntroduce("공개 소개");
    userRepository.saveAndFlush(user);
    List<User> relatedUsers = userDataGenerator.generateTestUsers(2);
    User followerA = relatedUsers.get(0);
    User followerB = relatedUsers.get(1);
    followRepository.saveAllAndFlush(List.of(
        new Follow(followerA, user),
        new Follow(followerB, user),
        new Follow(user, followerA)
    ));

    UserResponseDto userResponseDto =
        UserResponseDto.builder()
            .userEmail(user.getUserEmail())
            .name(user.getName())
            .introduce(user.getIntroduce())
            .gender(user.getGender())
            .birth(user.getBirth())
            .profileImage(user.getProfileImgUrl())
            .profileImageName(user.getProfileImgName())
            .publicUserId(user.getPublicUserId())
            .uploads(1)
            .followers(2)
            .following(1)
            .build();

    //when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<Void, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users")
            .httpMethod(HttpMethod.GET)
            .params(params(Map.of("userPublicID", user.getPublicUserId())))
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    //then

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(userResponseDto);
  }

  @Test
  @DisplayName("현재 인증된 사용자 정보 조회")
  void retrieveCurrentUser_success() throws Exception {
    // given
    User targetUser = userDataGenerator.generateTestUser(UUID.randomUUID() + "@test.com");
    targetUser.setIntroduce("현재 사용자 소개");
    userRepository.saveAndFlush(targetUser);
    // when
    UserResponseDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, UserResponseDto>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(targetUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getUserEmail()).isEqualTo(targetUser.getUserEmail());
    assertThat(response.getName()).isEqualTo(targetUser.getName());
    assertThat(response.getIntroduce()).isEqualTo(targetUser.getIntroduce());
    assertThat(response.getPublicUserId()).isEqualTo(targetUser.getPublicUserId());
  }

  @Test
  @DisplayName("이름과 한줄소개를 함께 수정한다")
  void updateProfile_updatesNameAndIntroduce() throws Exception {
    // given
    String updatedUserName = "updatedName";
    String updatedIntroduce = "새로운 소개";

    // when
    UserResponseDto result = updateProfile(Map.of(
        "name", updatedUserName,
        "introduce", updatedIntroduce));

    // then
    assertThat(result.getName()).isEqualTo(updatedUserName);
    assertThat(result.getIntroduce()).isEqualTo(updatedIntroduce);
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getName()).isEqualTo(updatedUserName);
    assertThat(updatedUser.getIntroduce()).isEqualTo(updatedIntroduce);
  }

  @Test
  @DisplayName("이름과 한줄소개를 각각 수정하고 빈 문자열로 소개를 지운다")
  void updateProfile_partialUpdatesPreserveOtherFieldAndClearIntroduce() throws Exception {
    user.setIntroduce("기존 소개");
    userRepository.saveAndFlush(user);

    UserResponseDto nameResult = updateProfile(Map.of("name", "변경된 이름"));
    assertThat(nameResult.getName()).isEqualTo("변경된 이름");
    assertThat(nameResult.getIntroduce()).isEqualTo("기존 소개");
    assertThat(userRepository.findByUserEmail(email).orElseThrow().getIntroduce())
        .isEqualTo("기존 소개");

    UserResponseDto introduceResult = updateProfile(Map.of("introduce", "변경된 소개"));
    assertThat(introduceResult.getName()).isEqualTo("변경된 이름");
    assertThat(introduceResult.getIntroduce()).isEqualTo("변경된 소개");
    assertThat(userRepository.findByUserEmail(email).orElseThrow().getName())
        .isEqualTo("변경된 이름");

    UserResponseDto cleared = updateProfile(Map.of("introduce", ""));
    assertThat(cleared.getIntroduce()).isEmpty();
    assertThat(userRepository.findByUserEmail(email).orElseThrow().getIntroduce()).isEmpty();
  }

  @Test
  @DisplayName("빈 요청과 null 입력은 이름과 한줄소개를 유지한다")
  void updateProfile_emptyAndNullFieldsPreserveValues() throws Exception {
    user.setIntroduce("기존 소개");
    userRepository.saveAndFlush(user);

    UserResponseDto emptyResult = updateProfile(Map.of());
    assertThat(emptyResult.getName()).isEqualTo(user.getName());
    assertThat(emptyResult.getIntroduce()).isEqualTo("기존 소개");

    Map<String, Object> body = new HashMap<>();
    body.put("name", null);
    body.put("introduce", null);
    UserResponseDto nullResult = updateProfile(body);

    assertThat(nullResult.getName()).isEqualTo(user.getName());
    assertThat(nullResult.getIntroduce()).isEqualTo("기존 소개");
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getName()).isEqualTo(user.getName());
    assertThat(updatedUser.getIntroduce()).isEqualTo("기존 소개");
  }

  @Test
  @DisplayName("줄바꿈을 포함한 100자 소개는 허용하고 101자는 거부한다")
  void updateProfile_introduceLengthBoundary() throws Exception {
    String introduce = "가".repeat(49) + "\n" + "나".repeat(50);

    UserResponseDto result = updateProfile(Map.of("introduce", introduce));
    assertThat(result.getIntroduce()).isEqualTo(introduce);
    assertThat(userRepository.findByUserEmail(email).orElseThrow().getIntroduce())
        .isEqualTo(introduce);

    Map<String, String> errors = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Map<String, String>, Map<String, String>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me")
            .httpMethod(HttpMethod.PATCH)
            .body(Map.of("introduce", "가".repeat(101)))
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    assertThat(errors).containsKey("introduce");
    assertThat(userRepository.findByUserEmail(email).orElseThrow().getIntroduce())
        .isEqualTo(introduce);
  }

  @Test
  @DisplayName("공백만 있는 이름을 거부한다")
  void updateProfile_blankNameReturnsBadRequest() throws Exception {
    Map<String, String> errors = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Map<String, String>, Map<String, String>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me")
            .httpMethod(HttpMethod.PATCH)
            .body(Map.of("name", " \n\t "))
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    assertThat(errors).containsKey("name");
    assertThat(userRepository.findByUserEmail(email).orElseThrow().getName())
        .isEqualTo(user.getName());
  }

  @Test
  @DisplayName("유저 프로필 이미지 수정")
  void updateUserProfileImageTest() throws Exception {
    // given
    String oldImageFileName = "old_image.png";
    user.setProfileImgName(oldImageFileName);
    user.setProfileImgUrl("https://test.com/" + oldImageFileName);
    userRepository.save(user);

    MockMultipartFile profileImage = new MockMultipartFile(
        "profileImage",
        "test-image.png",
        "image/png",
        "test image content".getBytes()
    );
    String expectedImageUrl = "https://test.com/test-image.png";

    Mockito.when(fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(expectedImageUrl);
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(expectedImageUrl);

    // when
    CustomResponse<UserResponseDto> response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/profile-image")
            .httpMethod(HttpMethod.PATCH)
            .formDataList(List.of(profileImage))
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    UserResponseDto result = response.getData();

    // 프로필 이미지 URL이 변경되었는지 검증
    assertThat(result.getProfileImage()).isEqualTo(expectedImageUrl);

    // 파일명이 저장되었는지 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getProfileImgUrl()).isEqualTo(expectedImageUrl);
    assertThat(updatedUser.getProfileImgName()).isNotNull();
    assertThat(updatedUser.getProfileImgName()).endsWith(".png");
    assertThat(updatedUser.getProfileImgName()).isNotEqualTo(oldImageFileName);

    // FileManager 호출 검증
    Mockito.verify(fileManager).uploadFile(any(byte[].class), contains("User"));
    Mockito.verify(fileManager, Mockito.times(1)).deleteFile(oldImageFileName);
    Mockito.verify(fileManager).resolveFilePath(updatedUser.getProfileImgName());
  }

  @Test
  @DisplayName("유저 성별 수정")
  void updateUserGenderTest() throws Exception {
    // given
    Gender updatedGender = Gender.male;
    UserGenderUpdateDto updateDto = UserGenderUpdateDto.builder()
        .gender(updatedGender)
        .build();

    // when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<UserGenderUpdateDto, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/gender")
            .httpMethod(HttpMethod.PATCH)
            .body(updateDto)
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    // 응답 검증
    assertThat(result.getGender()).isEqualTo(updatedGender);

    // DB 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getGender()).isEqualTo(updatedGender);
  }

  @Test
  @DisplayName("유저 생년월일 수정")
  void updateUserBirthTest() throws Exception {
    // given
    LocalDate updatedBirth = LocalDate.of(2000, 1, 1);
    UserBirthUpdateDto updateDto = UserBirthUpdateDto.builder()
        .birth(updatedBirth)
        .build();

    // when
    UserResponseDto result = mockMvcUtils.doAuthUnwrappedRequest(
        MockMvcRequestDto.<UserBirthUpdateDto, CustomResponse<UserResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/birth")
            .httpMethod(HttpMethod.PATCH)
            .body(updateDto)
            .clientDto(TestClientDto.fromEntity(user))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    // 응답 검증
    assertThat(result.getBirth()).isEqualTo(updatedBirth);

    // DB 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getBirth()).isEqualTo(updatedBirth);
  }
}
