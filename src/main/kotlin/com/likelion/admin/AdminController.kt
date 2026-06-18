package com.likelion.admin

import com.likelion.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService,
) {
    @PostMapping("/stores")
    @ResponseStatus(HttpStatus.CREATED)
    fun createStore(
        @Valid @RequestBody request: AdminStoreCreateRequest,
    ): ApiResponse<AdminStoreResponse> =
        ApiResponse.created(message = "매장이 등록되었습니다.", data = adminService.createStore(request))

    @PatchMapping("/stores/{storeId}")
    fun updateStore(
        @PathVariable storeId: Long,
        @RequestBody request: AdminStoreUpdateRequest,
    ): ApiResponse<AdminStoreResponse> =
        ApiResponse.ok(data = adminService.updateStore(storeId, request))

    @DeleteMapping("/stores/{storeId}")
    fun deactivateStore(
        @PathVariable storeId: Long,
    ): ApiResponse<AdminStoreDeactivateResponse> =
        ApiResponse.ok(message = "매장이 비활성화되었습니다.", data = adminService.deactivateStore(storeId))

    @PostMapping("/stores/{storeId}/menus")
    @ResponseStatus(HttpStatus.CREATED)
    fun createMenu(
        @PathVariable storeId: Long,
        @Valid @RequestBody request: AdminMenuCreateRequest,
    ): ApiResponse<AdminMenuResponse> =
        ApiResponse.created(message = "메뉴가 등록되었습니다.", data = adminService.createMenu(storeId, request))

    @PatchMapping("/menus/{menuId}")
    fun updateMenu(
        @PathVariable menuId: Long,
        @RequestBody request: AdminMenuUpdateRequest,
    ): ApiResponse<AdminMenuResponse> =
        ApiResponse.ok(data = adminService.updateMenu(menuId, request))

    @PostMapping("/stores/{storeId}/benefits")
    @ResponseStatus(HttpStatus.CREATED)
    fun createBenefit(
        @PathVariable storeId: Long,
        @Valid @RequestBody request: AdminBenefitCreateRequest,
    ): ApiResponse<AdminBenefitResponse> =
        ApiResponse.created(message = "혜택이 등록되었습니다.", data = adminService.createBenefit(storeId, request))

    @PatchMapping("/benefits/{benefitId}")
    fun updateBenefit(
        @PathVariable benefitId: Long,
        @RequestBody request: AdminBenefitUpdateRequest,
    ): ApiResponse<AdminBenefitResponse> =
        ApiResponse.ok(data = adminService.updateBenefit(benefitId, request))

    @PostMapping("/stores/{storeId}/qr/regenerate")
    fun regenerateQr(
        @PathVariable storeId: Long,
    ): ApiResponse<QrRegenerateResponse> =
        ApiResponse.ok(message = "QR 토큰이 재발급되었습니다.", data = adminService.regenerateQr(storeId))
}
