package com.komentum.catalog.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.catalog.dto.ComponentCatalogResponse;
import com.komentum.catalog.dto.ComponentType;
import com.komentum.global.utils.FileManager;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ComponentCatalogControllerTest {

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;

  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FileManager fileManager;

  @AfterEach
  public void tearDown() {
    testDataRemover.deleteAll();
  }

  void assertUserPostListResponseDto(ComponentCatalogResponse responseDto) {
    assertThat(responseDto.getComponentId()).isNotNull();
    assertThat(responseDto.getComponentType()).isIn(ComponentType.DESIGN, ComponentType.THEME);
    assertThat(responseDto.getPreviewImageUrl()).isNotBlank();
  }

  @Test
  @DisplayName("when send request, return user's theme and design components order by created date desc")
  void findCustomComponents_success() throws Exception {
    // stub
    BDDMockito.given(fileManager.resolveFilePath(BDDMockito.anyString()))
        .willReturn("http://mocked-url/1234567890");
    // given: 사용자마다 3개의 design component와 3개의 theme component 소유
    List<User> users = userScenarioSupport.builder()
        .withUsers(2)
        .build().users();
    List<DesignComponent> dc = designComponentScenarioSupport.builder(users)
        .withCountPerUser(3)
        .build().designComponents();
    var tc = themeComponentScenarioSupport.builder(users, dc)
        .withCountPerUser(3)
        .build();
    // given: page=0, size=5로 설정
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    // when
    User client = users.get(0);
    List<ComponentCatalogResponse> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ComponentCatalogResponse>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/users/me/custom-components")
            .clientDto(TestClientDto.fromEntity(client))
            .params(params)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(5);
    for (ComponentCatalogResponse res : response) {
      assertUserPostListResponseDto(res);
    }
  }

  @Test
  @DisplayName("비활성 디자인 컴포넌트는 제외하고 이를 참조하는 테마는 카탈로그에 보존한다")
  void findCustomComponents_excludesInactiveDesignsAndKeepsThemes() throws Exception {
    BDDMockito.given(fileManager.resolveFilePath(BDDMockito.anyString()))
        .willReturn("http://mocked-url/image.png");
    User client = userScenarioSupport.builder().withUsers(1).build().getFirstUser();
    List<DesignComponent> components = designComponentScenarioSupport.builder(List.of(client))
        .withCountPerUser(2).build().designComponents();
    var themes = themeComponentScenarioSupport.builder(List.of(client), components)
        .withCountPerUser(1).build().themeComponents();
    DesignComponent inactive = components.get(0);
    inactive.deactivate();
    designComponentRepository.saveAndFlush(inactive);

    List<ComponentCatalogResponse> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ComponentCatalogResponse>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/users/me/custom-components")
            .clientDto(TestClientDto.fromEntity(client))
            .params(TestParams.withPaging(0, 10))
            .responseType(new TypeReference<>() {
            })
            .build());

    assertThat(response).hasSize(2);
    assertThat(response.stream().filter(item -> item.getComponentType() == ComponentType.DESIGN))
        .extracting(ComponentCatalogResponse::getComponentId)
        .containsExactly(components.get(1).getDesignComponentId());
    assertThat(response.stream().filter(item -> item.getComponentType() == ComponentType.THEME))
        .singleElement().satisfies(item -> {
          assertThat(item.getComponentId()).isEqualTo(themes.get(0).getThemeComponentId());
          assertThat(item.getPreviewImageUrl()).isEqualTo("http://mocked-url/image.png");
        });
  }
}
