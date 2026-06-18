package com.likelion.favorite

import com.likelion.common.ApiResponse
import com.likelion.store.PageResponse
import com.likelion.store.StoreListItemResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class FavoriteController(
    private val favoriteService: FavoriteService,
) {
    @PostMapping("/stores/{storeId}/favorite")
    @ResponseStatus(HttpStatus.CREATED)
    fun addFavorite(
        @PathVariable storeId: Long,
    ): ApiResponse<FavoriteResultResponse> =
        ApiResponse.created(message = "즐겨찾기에 추가되었습니다.", data = favoriteService.addFavorite(storeId))

    @DeleteMapping("/stores/{storeId}/favorite")
    fun removeFavorite(
        @PathVariable storeId: Long,
    ): ApiResponse<FavoriteResultResponse> =
        ApiResponse.ok(message = "즐겨찾기가 해제되었습니다.", data = favoriteService.removeFavorite(storeId))

    @GetMapping("/me/favorites")
    fun getMyFavorites(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<PageResponse<StoreListItemResponse>> =
        ApiResponse.ok(message = "즐겨찾기 목록을 조회했습니다.", data = favoriteService.getMyFavorites(page, size))
}
