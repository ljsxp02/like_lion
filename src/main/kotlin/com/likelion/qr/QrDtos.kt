package com.likelion.qr

import jakarta.validation.constraints.NotBlank

data class QrVerifyRequest(
    @field:NotBlank(message = "QR 토큰은 필수입니다.")
    val qrToken: String,
)

data class QrVerifyResponse(
    val usageId: Long,
    val storeId: Long,
    val storeName: String,
    val verifiedAt: java.time.LocalDateTime,
)
