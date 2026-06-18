package com.likelion.admin

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class AdminStoreCreateRequest(
    @field:NotBlank(message = "매장명은 필수입니다.")
    val name: String,
    @field:NotBlank(message = "주소는 필수입니다.")
    val address: String,
    val location: String?,
    val contact: String?,
    val thumbnailUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
)

data class AdminStoreUpdateRequest(
    val name: String?,
    val address: String?,
    val location: String?,
    val contact: String?,
    val thumbnailUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
)

data class AdminStoreResponse(
    val storeId: Long,
    val name: String,
    val address: String,
    val isActive: Boolean,
)

data class AdminStoreDeactivateResponse(
    val storeId: Long,
    val isActive: Boolean,
)

data class AdminMenuCreateRequest(
    @field:NotBlank(message = "메뉴명은 필수입니다.")
    val name: String,
    val imageUrl: String?,
    @field:NotNull(message = "대표 메뉴 여부는 필수입니다.")
    val isRepresentative: Boolean,
    @field:NotNull(message = "노출 순서는 필수입니다.")
    val displayOrder: Int,
)

data class AdminMenuUpdateRequest(
    val name: String?,
    val imageUrl: String?,
    val isRepresentative: Boolean?,
    val displayOrder: Int?,
)

data class AdminMenuResponse(
    val menuId: Long,
    val storeId: Long,
    val name: String,
    val imageUrl: String?,
    val isRepresentative: Boolean,
    val displayOrder: Int,
)

data class AdminBenefitCreateRequest(
    @field:NotBlank(message = "혜택 제목은 필수입니다.")
    val title: String,
    @field:NotBlank(message = "혜택 설명은 필수입니다.")
    val description: String,
    @field:NotNull(message = "전체 학교 대상 여부는 필수입니다.")
    val isSchoolWide: Boolean,
    val collegeIds: List<Long>?,
    val departmentIds: List<Long>?,
    @field:NotNull(message = "혜택 시작일은 필수입니다.")
    val startDate: LocalDate,
    @field:NotNull(message = "혜택 종료일은 필수입니다.")
    val endDate: LocalDate,
)

data class AdminBenefitUpdateRequest(
    val title: String?,
    val description: String?,
    val isSchoolWide: Boolean?,
    val collegeIds: List<Long>?,
    val departmentIds: List<Long>?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isActive: Boolean?,
)

data class AdminBenefitResponse(
    val benefitId: Long,
    val storeId: Long,
    val title: String,
    val description: String,
    val isSchoolWide: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isActive: Boolean,
)

data class QrRegenerateResponse(
    val storeId: Long,
    val qrToken: String,
    val qrTokenIssuedAt: LocalDateTime,
)
