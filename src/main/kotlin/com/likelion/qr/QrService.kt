package com.likelion.qr

import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class QrService {
    fun verify(request: QrVerifyRequest): QrVerifyResponse {
        // TODO: QR 토큰 유효성, 중복 인증 제한, 혜택 사용 내역 저장 구현
        return QrVerifyResponse(
            usageId = 1,
            storeId = 1,
            storeName = "윤스쿡",
            verifiedAt = LocalDateTime.now(),
        )
    }
}
