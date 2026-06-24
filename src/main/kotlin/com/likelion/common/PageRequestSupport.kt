package com.likelion.common

import org.springframework.data.domain.PageRequest

/**
 * page/size 요청 값을 검증해 [PageRequest]로 변환한다.
 * page가 음수이거나 size가 0 이하이면 [ErrorCode.COMMON_400]을 던진다.
 * (검증 없이 PageRequest.of를 호출하면 IllegalArgumentException이 COMMON_500으로 노출된다.)
 */
fun pageRequestOf(page: Int, size: Int): PageRequest {
    if (page < 0 || size <= 0) {
        throw ApiException(ErrorCode.COMMON_400)
    }
    return PageRequest.of(page, size)
}
