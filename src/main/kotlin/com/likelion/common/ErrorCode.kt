package com.likelion.common

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    COMMON_400(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    COMMON_500(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    AUTH_001(HttpStatus.UNAUTHORIZED, "인증 토큰이 없거나 유효하지 않습니다."),
    AUTH_003(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    AUTH_004(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다."),
    AUTH_400(HttpStatus.BAD_REQUEST, "인증 요청 값이 올바르지 않습니다."),
    AUTH_401(HttpStatus.UNAUTHORIZED, "인증 정보가 일치하지 않습니다."),
    AUTH_409(HttpStatus.CONFLICT, "이미 처리된 인증 요청입니다."),

    USER_404(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_409(HttpStatus.CONFLICT, "이미 가입된 사용자입니다."),

    STORE_404(HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."),
    FAVORITE_409(HttpStatus.CONFLICT, "이미 즐겨찾기한 매장입니다."),

    QR_400(HttpStatus.BAD_REQUEST, "QR 토큰 형식이 올바르지 않습니다."),
    QR_404(HttpStatus.NOT_FOUND, "QR 토큰 또는 대상 매장을 찾을 수 없습니다."),
    QR_409(HttpStatus.CONFLICT, "이미 사용 처리된 QR 인증입니다."),

    ADMIN_STORE_404(HttpStatus.NOT_FOUND, "관리자 대상 매장을 찾을 수 없습니다."),
    ADMIN_MENU_404(HttpStatus.NOT_FOUND, "관리자 대상 메뉴를 찾을 수 없습니다."),
    ADMIN_BENEFIT_404(HttpStatus.NOT_FOUND, "관리자 대상 혜택을 찾을 수 없습니다."),
}
