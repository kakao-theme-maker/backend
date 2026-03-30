package com.komentum.theme.component.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.javafaker.Faker;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.ComponentTypeCreateRequest;
import com.komentum.theme.component.dto.ComponentTypeDto;
import com.komentum.theme.component.dto.ComponentTypeUpdateRequest;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.service.ComponentTypeSeeder;
import com.komentum.theme.utils.JsonUtils;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class ComponentTypeControllerTest {

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Autowired
  private ComponentTypeSeeder seeder;

  @Autowired
  private JsonUtils jsonUtils;

  private User client;

  @BeforeEach
  public void setUp() {
    client = userDataGenerator.generateTestUsers(1).get(0);
  }

  @AfterEach
  public void tearDown() {
    userDataGenerator.deleteAllUsers();
    componentTypeRepository.deleteAll();
  }

  public void assertComponentTypeDto(ComponentTypeDto response) {
    assertThat(response.getComponentTypeId()).isNotNull();
    ComponentType componentType = componentTypeRepository.findById(response.getComponentTypeId())
        .orElseThrow();
    compareResponseWithComponentType(response, componentType);
  }

  public void compareResponseWithComponentType(ComponentTypeDto response,
      ComponentType componentType) {
    assertThat(componentType)
        .extracting(
            ComponentType::getComponentName,
            ComponentType::getComponentPath,
            ComponentType::getPlatform,
            ComponentType::getExplain,
            ComponentType::getCreatedAt,
            ComponentType::getSizeX,
            ComponentType::getSizeY)
        .containsExactly(
            response.getComponentName(),
            response.getComponentPath(),
            response.getPlatform(),
            response.getExplain(),
            response.getCreatedAt(),
            response.getSizeX(),
            response.getSizeY());
  }

  public ComponentType createTestComponentType() {
    Faker faker = new Faker();
    return componentTypeRepository.save(ComponentType.builder()
        .explain(faker.lorem().paragraph())
        .platform(Platform.ANDROID)
        .componentName(UUID.randomUUID().toString())
        .componentPath(UUID.randomUUID().toString())
        .sizeX(faker.number().numberBetween(1, 9999))
        .sizeY(faker.number().numberBetween(1, 9999))
        .build());
  }

  @Test
  @DisplayName("ComponentType 생성 테스트")
  void createComponentType_success() throws Exception {
    // Given
    ComponentTypeCreateRequest request = ComponentTypeCreateRequest.builder()
        .explain("버튼 컴포넌트")
        .platform(Platform.ANDROID)
        .componentPath("/components/button")
        .componentName("Button")
        .sizeX(100)
        .sizeY(50)
        .build();
    // When
    ComponentTypeDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<ComponentTypeCreateRequest, ComponentTypeDto>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.POST)
            .path("/api/component-types")
            .clientDto(TestClientDto.fromEntity(client))
            .body(request)
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // Then
    assertThat(response)
        .extracting(
            ComponentTypeDto::getExplain,
            ComponentTypeDto::getPlatform,
            ComponentTypeDto::getComponentPath,
            ComponentTypeDto::getComponentName,
            ComponentTypeDto::getSizeX,
            ComponentTypeDto::getSizeY
        )
        .containsExactly(
            request.getExplain(),
            request.getPlatform(),
            request.getComponentPath(),
            request.getComponentName(),
            request.getSizeX(),
            request.getSizeY()
        );
    assertComponentTypeDto(response);
  }

  @Test
  @DisplayName("ComponentType 조회 테스트")
  void getComponentType_success() throws Exception {
    // Given
    ComponentType savedComponentType = createTestComponentType();
    // When
    ComponentTypeDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, ComponentTypeDto>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path(String.format("/api/component-types/%d", savedComponentType.getComponentTypeId()))
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // Then
    compareResponseWithComponentType(response, savedComponentType);
  }

  @Test
  @DisplayName("ComponentType 전체 조회 테스트")
  void getAllComponentTypes() throws Exception {
    // Given
    Map<Integer, ComponentType> expectedMap = IntStream.range(0, 5)
        .mapToObj(i -> createTestComponentType())
        .collect(Collectors.toMap(
            ComponentType::getComponentTypeId,
            Function.identity()));
    // When
    List<ComponentTypeDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ComponentTypeDto>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path("/api/component-types")
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // Then
    assertThat(response).hasSize(expectedMap.size());
    for (ComponentTypeDto res : response) {
      ComponentType expected = expectedMap.get(res.getComponentTypeId());
      assertThat(expected).isNotNull();
      compareResponseWithComponentType(res, expected);
    }
  }

  @Test
  @DisplayName("ComponentType 수정 테스트")
  void updateComponentType() throws Exception {
    // Given
    ComponentType savedComponent = createTestComponentType();
    ComponentTypeUpdateRequest updateRequest = ComponentTypeUpdateRequest.builder()
        .explain("수정된 컴포넌트")
        .platform(Platform.IOS)
        .componentPath("/updated")
        .componentName("UpdatedComponent")
        .sizeX(150)
        .sizeY(75)
        .build();
    // When
    ComponentTypeDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<ComponentTypeUpdateRequest, ComponentTypeDto>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path(String.format("/api/component-types/%d", savedComponent.getComponentTypeId()))
            .clientDto(TestClientDto.fromEntity(client))
            .body(updateRequest)
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // Then
    assertThat(response)
        .extracting(
            ComponentTypeDto::getExplain,
            ComponentTypeDto::getPlatform,
            ComponentTypeDto::getComponentPath,
            ComponentTypeDto::getComponentName,
            ComponentTypeDto::getSizeX,
            ComponentTypeDto::getSizeY
        )
        .containsExactly(
            updateRequest.getExplain(),
            updateRequest.getPlatform(),
            updateRequest.getComponentPath(),
            updateRequest.getComponentName(),
            updateRequest.getSizeX(),
            updateRequest.getSizeY()
        );
    assertComponentTypeDto(response);
  }

  @Test
  @DisplayName("시드 데이터 생성 성공 테스트")
  public void upsertComponentTypeWithSeed_success() throws Exception {
    // given
    List<ComponentTypeSeeder.ComponentTypeSeed> seeds = jsonUtils.readListAsType(
        ComponentTypeSeeder.COMPONENT_TYPE_JSON_PATH,
        ComponentTypeSeeder.ComponentTypeSeed.class);
    // when
    SeedResult response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/component-types/seed")
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getCreated()).isEqualTo(seeds.size());
    assertThat(response.getUpdated()).isEqualTo(0);
  }

  @Test
  @DisplayName("시드 데이터에 변경이 없다면, 갱신이 이루어지지 않는다")
  public void upsertComponentTypeWithSeed_shouldNotUpdateWhenSeedUnchanged() throws Exception {
    // given
    seeder.upsertComponentType();
    // when
    SeedResult response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/component-types/seed")
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getUpdated()).isEqualTo(0);
    assertThat(response.getCreated()).isEqualTo(0);
  }
}
