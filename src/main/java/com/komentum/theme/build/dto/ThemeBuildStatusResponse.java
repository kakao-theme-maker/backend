package com.komentum.theme.build.dto;

import com.komentum.theme.build.domain.ThemeBuildStatus;

/**
 * 테마 패키지 제작 작업 상태 응답이다.
 * 다운로드 URL은 상태 조회에서 발급하지 않으므로 항상 {@code null}이며, 명시적인 다운로드
 * API에서만 생성한다.
 */
public record ThemeBuildStatusResponse(
    ThemeBuildStatus status,
    String downloadUrl
) {

}
