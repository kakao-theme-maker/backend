package com.komentum.config;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.test.config.EnableTestProfile;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class SwaggerHiddenApiTest {

  private static final Map<String, List<String>> HIDDEN_OPERATIONS = Map.ofEntries(
      entry("/api/color-styles", List.of("post")),
      entry("/api/color-styles/{colorStyleId}", List.of("get", "put")),
      entry("/api/color-styles/seed", List.of("put")),
      entry("/api/component-types", List.of("post")),
      entry("/api/component-types/{componentTypeId}", List.of("get", "put")),
      entry("/api/component-types/seed", List.of("put")),
      entry("/api/platform-color-styles/seeds", List.of("put")),
      entry("/api/platform-component-types/seeds", List.of("put")),
      entry("/api/posts/{postId}/tags", List.of("get")),
      entry("/api/posts/tags/{tagId}", List.of("put", "delete")),
      entry("/api/categories", List.of("get", "post")),
      entry("/api/categories/{categoryId}", List.of("patch", "delete")),
      entry("/api/categories/{categoryId}/posts/{postId}", List.of("put", "delete")),
      entry("/api/bookmarks/posts/{postId}", List.of("put", "delete")),
      entry("/api/users/me/bookmarked-posts", List.of("get")),
      entry("/api/design-components/bookmarked", List.of("get")),
      entry("/api/themes/bookmarked", List.of("get")),
      entry("/api/posts/comments/{commentId}", List.of("get")),
      entry("/api/comments/{commentId}/like", List.of("get")),
      entry("/api/posts/{postId}/prefer", List.of("get")),
      entry("/api/users/me/gender", List.of("patch")),
      entry("/api/users/me/birth", List.of("patch"))
  );

  private static final Map<String, List<String>> VISIBLE_OPERATIONS = Map.ofEntries(
      entry("/api/color-styles", List.of("get")),
      entry("/api/component-types", List.of("get")),
      entry("/api/themes/default/seed", List.of("post")),
      entry("/api/themes", List.of("get")),
      entry("/api/themes/public", List.of("get")),
      entry("/api/themes/completed", List.of("get")),
      entry("/api/comments/{commentId}/like", List.of("post", "delete")),
      entry("/api/posts/{postId}/prefer", List.of("post", "delete")),
      entry("/api/posts/comments/{commentId}", List.of("put", "delete"))
  );

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("숨김 대상 API만 OpenAPI 문서에서 제외한다")
  void hideSelectedApiOperations() throws Exception {
    String responseBody = mockMvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    JsonNode paths = objectMapper.readTree(responseBody).path("paths");

    HIDDEN_OPERATIONS.forEach((path, methods) -> methods.forEach(method ->
        assertThat(paths.path(path).has(method))
            .as("%s %s should be hidden", method.toUpperCase(), path)
            .isFalse()));
    VISIBLE_OPERATIONS.forEach((path, methods) -> methods.forEach(method ->
        assertThat(paths.path(path).has(method))
            .as("%s %s should remain visible", method.toUpperCase(), path)
            .isTrue()));
  }
}
