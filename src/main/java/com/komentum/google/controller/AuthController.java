package com.komentum.google.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.komentum.google.dto.GoogleLoginRequestDto;
import com.komentum.google.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequestDto requestDto){
        GoogleIdToken.Payload payload = googleOAuthService.verifyToken(requestDto.idToken());
        if(payload == null){
            return ResponseEntity.badRequest().body("Invalid Google Token");
        }
        // 일단 구글 인증 성공했는 지 확인
        return ResponseEntity.ok(payload.getEmail());
    }
}
