package com.theme.dto;

import lombok.Builder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class KakaoRequestDto {
    public static class KakaoTokenRequest {
        final String grantType = "authorization_code";
        String clientId;
        String redirectUri;
        String code;
        String clientSecret;
        @Builder
        public KakaoTokenRequest(String clientId, String redirectUri, String code, String clientSecret) {
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.code = code;
            this.clientSecret = clientSecret;
        }
        public MultiValueMap<String, String> toMultiValueMap() {
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", grantType);
            map.add("client_id", clientId);
            map.add("redirect_uri", redirectUri);
            map.add("code", code);
            map.add("client_secret", clientSecret);
            return map;
        }
    }
}
