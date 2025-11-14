package com.komentum.google.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.komentum.google.dto.GoogleLoginRequestDto;
import com.komentum.google.service.GoogleOAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc
public class AuthControllerTest {

    @MockBean
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("구글 로그인 성공")
    public void googleLogin_success() throws Exception {
        String idToken = "valid_fake_token";

        GoogleIdToken.Payload payload = Mockito.mock(GoogleIdToken.Payload.class);
        Mockito.when(payload.getEmail())
                .thenReturn("test@gmail.com");

        Mockito.when(googleOAuthService.verifyToken(idToken))
                .thenReturn(payload);

        GoogleLoginRequestDto requestDto = new GoogleLoginRequestDto(idToken);
        String jsonBody = objectMapper.writeValueAsString(requestDto);

        mvc.perform(
                post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody)
        )
                .andExpect(status().isOk())
                .andExpect(content().string("test@gmail.com"));
    }
}
