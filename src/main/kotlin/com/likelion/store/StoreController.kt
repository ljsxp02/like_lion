package com.likelion.store

import com.likelion.common.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/stores")
class StoreController(
    private val storeService: StoreService,
) {
    @GetMapping
    fun getStores(
        @RequestParam(required = false) collegeId: Long?,
        @RequestParam(required = false) departmentId: Long?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) favoriteOnly: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PageResponse<StoreListItemResponse>> =
        ApiResponse.ok(
            message = "매장 리스트를 조회했습니다.",
            data = storeService.getStores(
                StoreSearchCondition(
                    collegeId = collegeId,
                    departmentId = departmentId,
                    keyword = keyword,
                    favoriteOnly = favoriteOnly,
                    page = page,
                    size = size,
                ),
            ),
        )

    @GetMapping("/map")
    fun getMapStores(
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(required = false) radiusMeters: Int?,
        @RequestParam(required = false) collegeId: Long?,
        @RequestParam(required = false) departmentId: Long?,
    ): ApiResponse<MapStoresResponse> =
        ApiResponse.ok(
            message = "지도용 매장 목록을 조회했습니다.",
            data = storeService.getMapStores(latitude, longitude, radiusMeters, collegeId, departmentId),
        )

    @GetMapping("/search")
    fun searchStores(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "10") limit: Int,
    ): ApiResponse<StoreAutocompleteListResponse> =
        ApiResponse.ok(message = "검색어 자동완성 결과를 조회했습니다.", data = storeService.searchStores(keyword, limit))

    @GetMapping("/{storeId}/summary")
    fun getStoreSummary(
        @PathVariable storeId: Long,
    ): ApiResponse<StoreSummaryResponse> =
        ApiResponse.ok(message = "매장 요약 정보를 조회했습니다.", data = storeService.getStoreSummary(storeId))

    @GetMapping("/{storeId}")
    fun getStoreDetail(
        @PathVariable storeId: Long,
    ): ApiResponse<StoreDetailResponse> =
        ApiResponse.ok(message = "매장 상세 정보를 조회했습니다.", data = storeService.getStoreDetail(storeId))
}
