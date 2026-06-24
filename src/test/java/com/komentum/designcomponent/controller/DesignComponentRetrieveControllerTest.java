package com.komentum.designcomponent.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@DisplayName("DesignComponent 조회 테스트")
class DesignComponentRetrieveControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 조회 테스트")
  void getDesignComponent() throws Exception {
    DesignComponent savedComponent = testUserComponent(
        "http://example.com/image.png", false, componentTypeA, componentTypeB);

    MockHttpServletRequestBuilder requestBuilder = get("/api/design-components/{id}",
        savedComponent.getDesignComponentId());

    MvcResult result = performAuthenticated(requestBuilder, status().isOk());
    DesignComponentDto response = objectMapper.readValue(
        result.getResponse().getContentAsString(), DesignComponentDto.class);

    assertThat(response.getDesignComponentId()).isEqualTo(savedComponent.getDesignComponentId());
    assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
    assertThat(response.getIsPublic()).isFalse();
    assertThat(response.getImageUrl()).isEqualTo("http://example.com/image.png");
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
        .andExpect(jsonPath("$.content[0].component_types.length()").value(1));
  }

  @Test
  @DisplayName("componentTypeId로 DesignComponent 목록 조회 테스트")
  void getDesignComponentsByComponentTypeId() throws Exception {
    DesignComponent componentAOnly = testUserComponent(
        "http://example.com/image-a.png", true, componentTypeA);
    DesignComponent componentAAndB = testUserComponent(
        "http://example.com/image-ab.png", true, componentTypeA, componentTypeB);
    testUserComponent("http://example.com/image-b.png", true, componentTypeB);

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
    testUserComponent("http://example.com/image-b.png", true, componentTypeB);

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

    testUserComponent("http://example.com/image1.png", true, componentTypeA);
    testUserComponent("http://example.com/image2.png", true, componentTypeA);
    testUserComponent("http://example.com/image3.png", true, componentTypeB);

    designComponentDataGenerator.generateDesignComponent(otherUser,
        "http://example.com/other1.png", true, List.of(componentTypeA));
    designComponentDataGenerator.generateDesignComponent(otherUser,
        "http://example.com/other2.png", true, List.of(componentTypeB));

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
            "http://example.com/image1.png",
            "http://example.com/image2.png",
            "http://example.com/image3.png"
        );
    assertThat(components).allMatch(
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
}
