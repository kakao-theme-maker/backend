package com.komentum.theme.component.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.ColorStyleCreateDto;
import com.komentum.theme.component.dto.ColorStyleResponse;
import com.komentum.theme.component.dto.ColorStyleUpdateRequest;
import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.service.ColorStyleSeeder;
import com.komentum.theme.component.service.ColorStyleSeeder.ColorStyleSeed;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class ColorStyleControllerTest {

  @Autowired
  private ColorStyleRepository colorStyleRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Autowired
  private ColorStyleSeeder seeder;

  @Autowired
  private JsonUtils jsonUtils;

  private void assertColorStyleResponse(ColorStyleResponse response) {
    assertThat(response.getColorStyleId()).isNotNull();
    ColorStyle colorStyle = colorStyleRepository.findById(response.getColorStyleId())
        .orElseThrow();
    compareResponseWithColorStyle(response, colorStyle);
  }

  private void compareResponseWithColorStyle(ColorStyleResponse response, ColorStyle colorStyle) {
    assertThat(colorStyle)
        .extracting(
            ColorStyle::getStylePropsName,
            ColorStyle::getStyleSheetPath,
            ColorStyle::getStyleElementName,
            ColorStyle::getExplain,
            ColorStyle::getPlatform
        )
        .containsExactly(
            response.getStylePropsName(),
            response.getStyleSheetPath(),
            response.getStyleElementName(),
            response.getExplain(),
            response.getPlatform()
        );
  }

  private ColorStyle createColorStyle() {
    Faker faker = new Faker();
    return colorStyleRepository.save(ColorStyle.builder()
        .explain(faker.color().name())
        .platform(Platform.ANDROID)
        .styleSheetPath(UUID.randomUUID().toString())
        .styleElementName(UUID.randomUUID().toString())
        .stylePropsName(UUID.randomUUID().toString())
        .build());
  }

  User client;

  @BeforeEach
  public void setUp() {
    client = userDataGenerator.generateTestUsers(1).get(0);
  }

  @AfterEach
  public void tearDown() {
    userDataGenerator.deleteAllUsers();
    colorStyleRepository.deleteAll();
  }

  @Test
  @DisplayName("ColorStyle 생성 테스트")
  void createColorStyle_success() throws Exception {
    // Given
    ColorStyleCreateDto createRequest = ColorStyleCreateDto.builder()
        .explain("기본 색상 스타일")
        .platform(Platform.ANDROID)
        .styleSheetPath("/styles/colors.css")
        .styleElementName("primaryColor")
        .stylePropsName("color")
        .build();
    MockHttpServletRequestBuilder requestBuilder = mockMvcUtils.addAuthentication(
        MockMvcRequestBuilders
            .post("/api/color-styles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)),
        TestClientDto.fromEntity(client)
    );
    // When
    ColorStyleResponse response = mockMvcUtils.parseResponse(
        mockMvc.perform(requestBuilder).andExpect(status().isOk()),
        new TypeReference<>() {
        });
    // then
    assertThat(response)
        .extracting(
            ColorStyleResponse::getStyleSheetPath,
            ColorStyleResponse::getStylePropsName,
            ColorStyleResponse::getStyleElementName,
            ColorStyleResponse::getPlatform,
            ColorStyleResponse::getExplain
        )
        .containsExactly(
            createRequest.getStyleSheetPath(),
            createRequest.getStylePropsName(),
            createRequest.getStyleElementName(),
            createRequest.getPlatform(),
            createRequest.getExplain()
        );
    assertColorStyleResponse(response);
  }

  @Test
  @DisplayName("ColorStyle 조회 테스트")
  void getColorStyle_success() throws Exception {
    // Given
    ColorStyle savedColorStyle = createColorStyle();
    MockHttpServletRequestBuilder requestBuilder = mockMvcUtils.addAuthentication(
        MockMvcRequestBuilders
            .get("/api/color-styles/{id}", savedColorStyle.getColorStyleId())
            .contentType(MediaType.APPLICATION_JSON),
        TestClientDto.fromEntity(client)
    );
    // When
    ColorStyleResponse response = mockMvcUtils.parseResponse(
        mockMvc.perform(requestBuilder).andExpect(status().isOk()),
        new TypeReference<>() {
        });
    // then
    compareResponseWithColorStyle(response, savedColorStyle);
  }

  @Test
  @DisplayName("ColorStyle 전체 조회 테스트")
  void getAllColorStyles_success() throws Exception {
    // Given
    Map<Integer, ColorStyle> expectedMap = IntStream.range(0, 5)
        .mapToObj(i -> createColorStyle())
        .collect(Collectors.toMap(
            ColorStyle::getColorStyleId,
            Function.identity()));
    // When
    List<ColorStyleResponse> responses = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ColorStyleResponse>>builder()
            .mockMvc(mockMvc)
            .path("/api/color-styles")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(responses).hasSize(expectedMap.size());
    for (ColorStyleResponse res : responses) {
      ColorStyle expected = expectedMap.get(res.getColorStyleId());
      assertThat(expected).isNotNull();
      compareResponseWithColorStyle(res, expected);
    }
  }

  @Test
  @DisplayName("ColorStyle 수정 테스트")
  void updateColorStyle_success() throws Exception {
    // Given
    ColorStyle savedColorStyle = createColorStyle();
    ColorStyleUpdateRequest updateRequest = ColorStyleUpdateRequest.builder()
        .explain(UUID.randomUUID().toString())
        .platform(Platform.IOS)
        .styleSheetPath(UUID.randomUUID().toString())
        .styleElementName(UUID.randomUUID().toString())
        .stylePropsName(UUID.randomUUID().toString())
        .build();
    // When
    ColorStyleResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<ColorStyleUpdateRequest, ColorStyleResponse>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/color-styles/%d", savedColorStyle.getColorStyleId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .body(updateRequest)
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response)
        .extracting(
            ColorStyleResponse::getStyleSheetPath,
            ColorStyleResponse::getStylePropsName,
            ColorStyleResponse::getStyleElementName,
            ColorStyleResponse::getPlatform,
            ColorStyleResponse::getExplain
        )
        .containsExactly(
            updateRequest.getStyleSheetPath(),
            updateRequest.getStylePropsName(),
            updateRequest.getStyleElementName(),
            updateRequest.getPlatform(),
            updateRequest.getExplain()
        );
    assertColorStyleResponse(response);
  }

  @Test
  @DisplayName("color style 시드 데이터를 생성한다")
  public void upsertColorStyleBySeed_onlyInsert() throws Exception {
    // given
    List<ColorStyleSeed> seeds = jsonUtils.readListAsType(
        ColorStyleSeeder.COLOR_STYLE_JSON_PATH,
        ColorStyleSeed.class);
    // when
    SeedResult response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/color-styles/seed")
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getCreated()).isEqualTo(seeds.size());
  }

  @Test
  @DisplayName("color style 시드 데이터를 주입 시, 시드에 변경이 없으면 갱신하지 않는다")
  public void upsertColorStyleBySeed_shouldNotUpdateWhenSeedUnchanged() throws Exception {
    // given
    seeder.upsertColorStyleSeed();
    // when
    SeedResult response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/color-styles/seed")
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
