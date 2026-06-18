package com.likelion.mypage

import com.likelion.common.ApiResponse
import com.likelion.store.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me")
class MyPageController(
    private val myPageService: MyPageService,
) {
    @GetMapping
    fun getMyPage(): ApiResponse<MyPageResponse> =
        ApiResponse.ok(message = "마이페이지를 조회했습니다.", data = myPageService.getMyPage())

    @GetMapping("/benefit-usages")
    fun getMyBenefitUsages(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PageResponse<BenefitUsageResponse>> =
        ApiResponse.ok(message = "혜택 사용 내역을 조회했습니다.", data = myPageService.getMyBenefitUsages(page, size))
}
