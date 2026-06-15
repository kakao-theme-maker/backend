package com.komentum.theme.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import com.komentum.global.utils.FileManager;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.enums.TypeCode;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class DesignComponentServiceTest {

  @Autowired
  private DesignComponentService designComponentService;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @MockitoBean
  private FileManager fileManager;

  private User user;
  private ComponentType componentType;

  @BeforeEach
  void setUp() {
    designComponentRepository.deleteAll();
    componentTypeRepository.deleteAll();
    userDataGenerator.deleteAllUsers();
    reset(fileManager);

    user = userDataGenerator.generateTestUser("service-test@example.com");
    componentType = componentTypeRepository.save(ComponentType.builder()
        .typeCode(TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE)
        .name("service test component type")
        .explain("service test component type explain")
        .build());
  }

  @AfterEach
  void tearDown() {
    designComponentRepository.deleteAll();
    componentTypeRepository.deleteAll();
    userDataGenerator.deleteAllUsers();
    reset(fileManager);
  }

  @Test
  @DisplayName("다중 업로드 중 하나 실패하면 전체 롤백되고 선업로드 파일이 정리된다")
  void createDesignComponents_allOrNothing_whenOneFails() {
    CreateDesignComponentRequest request = CreateDesignComponentRequest.builder()
        .isPublic(true)
        .componentTypeIds(java.util.List.of(componentType.getComponentTypeId()))
        .build();
    MockMultipartFile firstFile = new MockMultipartFile(
        "files",
        "first.png",
        MediaType.IMAGE_PNG_VALUE,
        "first-file".getBytes()
    );
    MockMultipartFile secondFile = new MockMultipartFile(
        "files",
        "second.png",
        MediaType.IMAGE_PNG_VALUE,
        "second-file".getBytes()
    );

    given(fileManager.uploadFile(any(byte[].class), anyString()))
        .willReturn("https://s3.example.com/first-upload.png")
        .willThrow(new RuntimeException("forced upload failure"));
    given(fileManager.convertUrlToFileName(anyString()))
        .willAnswer(invocation -> invocation.getArgument(0, String.class)
            .replace("https://s3.example.com/", ""));

    assertThatThrownBy(
        () -> designComponentService.createDesignComponents(request, java.util.List.of(firstFile,
            secondFile), user))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("forced upload failure");

    assertThat(designComponentRepository.count()).isZero();
    verify(fileManager).deleteFile("first-upload.png");
  }
}
