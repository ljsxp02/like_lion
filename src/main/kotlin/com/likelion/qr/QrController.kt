package com.likelion.qr

import com.likelion.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/qr")
class QrController(
    private val qrService: QrService,
) {
    @PostMapping("/verify")
    fun verify(
        @Valid @RequestBody request: QrVerifyRequest,
    ): ApiResponse<QrVerifyResponse> =
        ApiResponse.ok(message = "QR 인증이 완료되었습니다.", data = qrService.verify(request))
}
