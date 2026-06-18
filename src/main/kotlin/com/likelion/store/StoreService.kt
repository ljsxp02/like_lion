package com.likelion.store

import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StoreService {
    private val sampleStores = listOf(
        StoreListItemResponse(
            storeId = 1,
            name = "윤스쿡",
            thumbnailUrl = "https://image.example.com/store1.jpg",
            address = "서울 노원구 광운로 20",
            description = "광운대생 인증 시 10% 할인",
            isFavorite = false,
        ),
    )

    fun getStores(condition: StoreSearchCondition): PageResponse<StoreListItemResponse> =
        PageResponse(
            content = sampleStores,
            page = condition.page,
            size = condition.size,
            totalElements = sampleStores.size.toLong(),
            totalPages = 1,
            hasNext = false,
        )

    fun getMapStores(
        latitude: Double?,
        longitude: Double?,
        radiusMeters: Int?,
        collegeId: Long?,
        departmentId: Long?,
    ): MapStoresResponse =
        MapStoresResponse(
            stores = listOf(
                MapStoreResponse(
                    storeId = 1,
                    name = "윤스쿡",
                    thumbnailUrl = "https://image.example.com/store1.jpg",
                    address = "서울 노원구 광운로 20",
                    latitude = latitude ?: 37.6194,
                    longitude = longitude ?: 127.0598,
                    categories = listOf("음식점"),
                ),
            ),
        )

    fun searchStores(keyword: String, limit: Int): StoreAutocompleteListResponse =
        StoreAutocompleteListResponse(
            suggestions = sampleStores
                .filter { it.name.contains(keyword, ignoreCase = true) }
                .take(limit)
                .map { StoreAutocompleteResponse(storeId = it.storeId, name = it.name, address = it.address) },
        )

    fun getStoreSummary(storeId: Long): StoreSummaryResponse =
        StoreSummaryResponse(
            storeId = storeId,
            name = "윤스쿡",
            thumbnailUrl = "https://image.example.com/store1.jpg",
            address = "서울 노원구 광운로 20",
            description = "광운대생 인증 시 10% 할인",
            isFavorite = false,
        )

    fun getStoreDetail(storeId: Long): StoreDetailResponse =
        StoreDetailResponse(
            storeId = storeId,
            name = "윤스쿡",
            address = "서울 노원구 광운로 20",
            location = "광운대학교 정문 근처",
            contact = "02-123-4567",
            thumbnailUrl = "https://image.example.com/store1.jpg",
            description = "광운대 주변 제휴 매장",
            isSchoolWide = true,
            benefits = listOf(
                BenefitResponse(
                    benefitId = 5,
                    title = "광운대생 10% 할인",
                    description = "학생 인증 시 전 메뉴 10% 할인",
                    startDate = LocalDate.of(2026, 3, 1),
                    endDate = LocalDate.of(2026, 12, 31),
                    isActive = true,
                ),
            ),
            menus = listOf(
                MenuResponse(
                    menuId = 10,
                    name = "아이스 아메리카노",
                    imageUrl = "https://example.com/menu/americano.png",
                    isRepresentative = true,
                    displayOrder = 1,
                ),
            ),
            isFavorite = false,
        )
}
