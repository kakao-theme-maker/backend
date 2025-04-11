package com.theme.dto;

import lombok.Getter;

public class KakaoResponseDto {
    @Getter
    public static class KakaoTokenResponse {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private int expires_in;
        private String scope;
        private int refresh_token_expires_in;
    }

    @Getter
    public static class KakaoUserInfoResponse {
        private KakaoAccount kakao_account;
        @Getter
        public class KakaoAccount {
            private String email;
            private Profile profile;
            private String birthyear;
            private String birthday;
            private String gender;
            @Getter
            public class Profile {
                private String profile_image_url;
            }
        }
    }
}
