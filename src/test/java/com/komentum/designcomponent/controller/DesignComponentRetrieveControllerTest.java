package com.komentum.designcomponent.controller;

import static com.komentum.test.fixture.component.DesignComponentRequestFixture.imageUrlOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.global.security.UserRole;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@DisplayName("DesignComponent 조회 테스트")
class DesignComponentRetrieveControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 조회 테스트")
  void getDesignComponent() throws Exception {
    DesignComponent savedComponent = testUserComponent(
        "image.png", false, componentTypeA, componentTypeB);

    MockHttpServletRequestBuilder requestBuilder = get("/api/design-components/{id}",
        savedComponent.getDesignComponentId());

    MvcResult result = performAuthenticated(requestBuilder, status().isOk());
    DesignComponentDto response = objectMapper.readValue(
        result.getResponse().getContentAsString(), DesignComponentDto.class);

    assertThat(response.getDesignComponentId()).isEqualTo(savedComponent.getDesignComponentId());
    assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
    assertThat(response.getIsPublic()).isFalse();
    assertThat(response.getImageUrl()).isEqualTo(imageUrlOf("image.png"));
    assertThat(response.getCreatedAt()).isNotNull();
    assertThat(response.getUpdatedAt()).isNotNull();
    assertThat(response.getComponentTypes())
        .extracting("componentTypeId")
        .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
            componentTypeB.getComponentTypeId());
  }

  @Test
  @DisplayName("DesignComponent 전체 조회 테스트")
  void getAllDesignComponents() throws Exception {
    designComponentDataGenerator.generateDesignComponents(
        userDataGenerator.generateTestUsers(5), 4, List.of(componentTypeA));

    MockHttpServletRequestBuilder requestBuilder = get("/api/design-components");

    mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(20))
        .andExpect(jsonPath("$.content[0].componentTypes.length()").value(1));
  }

  @Test
  @DisplayName("componentTypeId로 DesignComponent 목록 조회 테스트")
  void getDesignComponentsByComponentTypeId() throws Exception {
    DesignComponent componentAOnly = testUserComponent(
        "image-a.png", true, componentTypeA);
    DesignComponent componentAAndB = testUserComponent(
        "image-ab.png", true, componentTypeA, componentTypeB);
    testUserComponent("image-b.png", true, componentTypeB);

    MockHttpServletRequestBuilder requestBuilder = get(
        "/api/design-components/component-types/{componentTypeId}",
        componentTypeA.getComponentTypeId());

    MvcResult result = performAuthenticated(requestBuilder, status().isOk());

    DesignComponentDto[] responses = objectMapper.readValue(
        result.getResponse().getContentAsString(), DesignComponentDto[].class);

    assertThat(responses).hasSize(2);
    assertThat(responses)
        .extracting(DesignComponentDto::getDesignComponentId)
        .containsExactlyInAnyOrder(
            componentAOnly.getDesignComponentId(),
            componentAAndB.getDesignComponentId()
        );
    assertThat(responses).allSatisfy(response ->
        assertThat(response.getComponentTypes())
            .extracting("componentTypeId")
            .contains(componentTypeA.getComponentTypeId()));
  }

  @Test
  @DisplayName("존재하지 않는 componentTypeId 조회 시 404 반환")
  void getDesignComponentsByUnknownComponentTypeId() throws Exception {
    MockHttpServletRequestBuilder requestBuilder = get(
        "/api/design-components/component-types/{componentTypeId}", 999999);

    mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("componentType에 속한 DesignComponent가 없으면 빈 리스트 반환")
  void getDesignComponentsByComponentTypeId_emptyResult() throws Exception {
    testUserComponent("image-b.png", true, componentTypeB);

    MockHttpServletRequestBuilder requestBuilder = get(
        "/api/design-components/component-types/{componentTypeId}",
        componentTypeA.getComponentTypeId());

    MvcResult result = performAuthenticated(requestBuilder, status().isOk());

    DesignComponentDto[] responses = objectMapper.readValue(
        result.getResponse().getContentAsString(), DesignComponentDto[].class);
    assertThat(responses).isEmpty();
  }

  @Test
  @DisplayName("특정 사용자의 DesignComponent 목록 조회 테스트")
  void getDesignComponentsByPublicUserId() throws Exception {
    User otherUser = createOtherUser("other@test.com");

    testUserComponent("image1.png", true, componentTypeA);
    testUserComponent("image2.png", true, componentTypeA);
    testUserComponent("image3.png", true, componentTypeB);

    designComponentDataGenerator.generateDesignComponent(otherUser,
        "other1.png", true, List.of(componentTypeA));
    designComponentDataGenerator.generateDesignComponent(otherUser,
        "other2.png", true, List.of(componentTypeB));

    MockHttpServletRequestBuilder requestBuilder = get(
        "/api/design-components/user/{publicUserId}",
        testUser.getPublicUserId());

    MvcResult result = mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andReturn();

    String responseContent = result.getResponse().getContentAsString();
    DesignComponentDto[] components = objectMapper.readValue(responseContent,
        DesignComponentDto[].class);

    assertThat(components).hasSize(3);
    assertThat(components)
        .extracting(DesignComponentDto::getImageUrl)
        .containsExactlyInAnyOrder(
            imageUrlOf("image1.png"),
            imageUrlOf("image2.png"),
            imageUrlOf("image3.png")
        );
    assertThat(components).allMatch(
        dto -> dto.getPublicUserId().equals(testUser.getPublicUserId()));
  }

  @Test
  @DisplayName("when send request, retrieve uploaded design components")
  public void findUploadedDesignComponent_success() throws Exception {
    // given
    User otherUser = createOtherUser("other@test.com");

    testUserComponent("image1.png", true, componentTypeA);
    testUserComponent("image2.png", true, componentTypeA);
    testUserComponent("image3.png", true, componentTypeB);

    designComponentDataGenerator.generateDesignComponent(otherUser,
        "other1.png", true, List.of(componentTypeA));
    designComponentDataGenerator.generateDesignComponent(otherUser,
        "other2.png", true, List.of(componentTypeB));

    // when
    List<DesignComponentDto> res = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<DesignComponentDto>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-components/uploaded")
            .clientDto(TestClientDto.fromEntity(testUser))
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    // then
    assertThat(res).hasSize(3);
    assertThat(res)
        .extracting(DesignComponentDto::getImageUrl)
        .containsExactlyInAnyOrder(
            imageUrlOf("image1.png"),
            imageUrlOf("image2.png"),
            imageUrlOf("image3.png")
        );
    assertThat(res)
        .extracting(DesignComponentDto::getImageUrl)
        .doesNotContain(
            imageUrlOf("other1.png"),
            imageUrlOf("other2.png")
        );
    assertThat(res).allMatch(
        dto -> dto.getPublicUserId().equals(testUser.getPublicUserId()));
  }

  @Test
  @DisplayName("when send request, retrieve bookmarked design components")
  public void findBookmarkedDesignComponent_success() throws Exception {
    // given
    // 유저당 3개의 design component
    Map<User, List<DesignComponent>> designComponentMap = designComponentScenarioSupport
        .builder(List.of(testUser))
        .withCountPerUser(3)
        .build()
        .designComponents().stream()
        .collect(Collectors.groupingBy(DesignComponent::getUser));
    // 유저당 2개의 design boards를 생성하고, 모든 design board 북마크
    // design board는 각각 3개의 design component를 갖는다
    var postResult = postScenarioSupport.builder(List.of(testUser))
        .withDesignBoardsPerUser(4, designComponentMap)
        .withBookmarkRatio(1)
        .build();
    // when
    List<DesignComponentDto> res = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<DesignComponentDto>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/design-components/bookmarked")
            .clientDto(TestClientDto.fromEntity(testUser))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(res).hasSize(3);
  }

  @ParameterizedTest
  @EnumSource(value = UserRole.class, names = {"USER", "ADMIN"})
  @DisplayName("소유자와 관리자 모두 비활성 컴포넌트 단건 조회는 404를 받는다")
  void getInactiveDesignComponent(UserRole role) throws Exception {
    DesignComponent inactive = testUserComponent();
    inactive.deactivate();
    designComponentRepository.flush();
    authenticateAs(testUser, role);

    performAuthenticated(get("/api/design-components/{id}", inactive.getDesignComponentId()),
        status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {"user", "uploaded", "component-type"})
  @DisplayName("사용자·업로드·컴포넌트 타입 목록에서 비활성 컴포넌트를 제외한다")
  void componentLists_excludeInactiveComponents(String listType) throws Exception {
    DesignComponent active = testUserComponent("active.png", true, componentTypeA);
    DesignComponent inactive = testUserComponent("inactive.png", true, componentTypeA);
    inactive.deactivate();
    designComponentRepository.flush();
    String path = switch (listType) {
      case "user" -> "/api/design-components/user/" + testUser.getPublicUserId();
      case "uploaded" -> "/api/design-components/uploaded";
      default -> "/api/design-components/component-types/" + componentTypeA.getComponentTypeId();
    };

    MvcResult result = performAuthenticated(get(path), status().isOk());
    DesignComponentDto[] response = objectMapper.readValue(
        result.getResponse().getContentAsString(), DesignComponentDto[].class);

    assertThat(response).extracting(DesignComponentDto::getDesignComponentId)
        .containsExactly(active.getDesignComponentId());
  }

  @Test
  @DisplayName("페이지 내용과 전체 개수 모두 비활성 컴포넌트를 제외한다")
  void componentPages_excludeInactiveComponentsFromContentAndCount() throws Exception {
    DesignComponent first = testUserComponent("first.png", true, componentTypeA);
    testUserComponent("inactive-first.png", true, componentTypeA).deactivate();
    DesignComponent second = testUserComponent("second.png", true, componentTypeA);
    testUserComponent("inactive-second.png", true, componentTypeA).deactivate();
    DesignComponent third = testUserComponent("third.png", true, componentTypeA);
    designComponentRepository.flush();

    mockMvc.perform(mockMvcUtils.addAuthentication(get("/api/design-components")
            .param("page", "0").param("size", "2").param("sort", "designComponentId,asc"),
        testClient))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.totalPages").value(2))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].designComponentId").value(first.getDesignComponentId()))
        .andExpect(jsonPath("$.content[1].designComponentId").value(second.getDesignComponentId()));
    mockMvc.perform(mockMvcUtils.addAuthentication(get("/api/design-components")
            .param("page", "1").param("size", "2").param("sort", "designComponentId,asc"),
        testClient))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].designComponentId").value(third.getDesignComponentId()));
    mockMvc.perform(mockMvcUtils.addAuthentication(get("/api/design-components")
            .param("page", "2").param("size", "2"), testClient))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  @DisplayName("북마크 컴포넌트 목록은 기존 게시글의 비활성 컴포넌트를 제외한다")
  void bookmarkedComponents_excludeInactiveComponents() throws Exception {
    List<DesignComponent> components = designComponentScenarioSupport.builder(List.of(testUser))
        .withCountPerUser(2).build().designComponents();
    postScenarioSupport.builder(List.of(testUser))
        .withDesignBoardsPerUser(1, Map.of(testUser, components))
        .withBookmarkRatio(1).build();
    components.get(0).deactivate();
    designComponentRepository.flush();

    MvcResult result = performAuthenticated(get("/api/design-components/bookmarked"),
        status().isOk());
    DesignComponentDto[] response = objectMapper.readValue(
        result.getResponse().getContentAsString(), DesignComponentDto[].class);

    assertThat(response).extracting(DesignComponentDto::getDesignComponentId)
        .containsExactly(components.get(1).getDesignComponentId());
  }
}
