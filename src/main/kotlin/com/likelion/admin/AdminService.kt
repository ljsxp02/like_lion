package com.likelion.admin

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AdminService {
    fun createStore(request: AdminStoreCreateRequest): AdminStoreResponse {
        // TODO: 관리자 권한 확인, 좌표 입력 방식 확정, 매장 저장 구현
        return AdminStoreResponse(storeId = 1, name = request.name, address = request.address, isActive = true)
    }

    fun updateStore(storeId: Long, request: AdminStoreUpdateRequest): AdminStoreResponse {
        // TODO: PATCH null 필드 처리 정책 확정 후 구현
        return AdminStoreResponse(
            storeId = storeId,
            name = request.name ?: "윤스쿡",
            address = request.address ?: "서울 노원구 광운로 20",
            isActive = true,
        )
    }

    fun deactivateStore(storeId: Long): AdminStoreDeactivateResponse {
        // TODO: 소프트 삭제 정책으로 비활성화 구현
        return AdminStoreDeactivateResponse(storeId = storeId, isActive = false)
    }

    fun createMenu(storeId: Long, request: AdminMenuCreateRequest): AdminMenuResponse {
        // TODO: 매장 소유자/관리자 권한 확인 후 메뉴 저장 구현
        return AdminMenuResponse(
            menuId = 10,
            storeId = storeId,
            name = request.name,
            imageUrl = request.imageUrl,
            isRepresentative = request.isRepresentative,
            displayOrder = request.displayOrder,
        )
    }

    fun updateMenu(menuId: Long, request: AdminMenuUpdateRequest): AdminMenuResponse =
        AdminMenuResponse(
            menuId = menuId,
            storeId = 1,
            name = request.name ?: "아이스 아메리카노",
            imageUrl = request.imageUrl ?: "https://example.com/menu/americano.png",
            isRepresentative = request.isRepresentative ?: true,
            displayOrder = request.displayOrder ?: 1,
        )

    fun createBenefit(storeId: Long, request: AdminBenefitCreateRequest): AdminBenefitResponse {
        // TODO: 매장별 복수 혜택 허용 정책 확정 후 구현
        return AdminBenefitResponse(
            benefitId = 5,
            storeId = storeId,
            title = request.title,
            description = request.description,
            isSchoolWide = request.isSchoolWide,
            startDate = request.startDate,
            endDate = request.endDate,
            isActive = true,
        )
    }

    fun updateBenefit(benefitId: Long, request: AdminBenefitUpdateRequest): AdminBenefitResponse {
        // TODO: 기간 만료 혜택 비활성화 정책 확정 후 구현
        return AdminBenefitResponse(
            benefitId = benefitId,
            storeId = 1,
            title = request.title ?: "광운대생 10% 할인",
            description = request.description ?: "학생 인증 시 전 메뉴 10% 할인",
            isSchoolWide = request.isSchoolWide ?: true,
            startDate = request.startDate ?: LocalDate.of(2026, 3, 1),
            endDate = request.endDate ?: LocalDate.of(2026, 12, 31),
            isActive = request.isActive ?: true,
        )
    }

    fun regenerateQr(storeId: Long): QrRegenerateResponse {
        // TODO: 기존 QR 무효화 후 새 토큰 발급 구현
        return QrRegenerateResponse(
            storeId = storeId,
            qrToken = "new-qr-token-example",
            qrTokenIssuedAt = LocalDateTime.now(),
        )
    }
}
