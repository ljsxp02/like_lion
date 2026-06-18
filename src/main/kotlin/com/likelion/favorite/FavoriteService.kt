package com.likelion.favorite

import com.likelion.store.PageResponse
import com.likelion.store.StoreListItemResponse
import org.springframework.stereotype.Service

@Service
class FavoriteService {
    fun addFavorite(storeId: Long): FavoriteResultResponse {
        // TODO: 로그인 사용자 기준 중복 즐겨찾기 정책 확정 후 구현
        return FavoriteResultResponse(storeId = storeId, isFavorite = true)
    }

    fun removeFavorite(storeId: Long): FavoriteResultResponse {
        // TODO: 멱등 삭제 여부 확정 후 구현
        return FavoriteResultResponse(storeId = storeId, isFavorite = false)
    }

    fun getMyFavorites(page: Int, size: Int): PageResponse<StoreListItemResponse> =
        PageResponse(
            content = listOf(
                StoreListItemResponse(
                    storeId = 1,
                    name = "윤스쿡",
                    thumbnailUrl = "https://image.example.com/store1.jpg",
                    address = "서울 노원구 광운로 20",
                    description = "광운대생 인증 시 10% 할인",
                    isFavorite = true,
                ),
            ),
            page = page,
            size = size,
            totalElements = 1,
            totalPages = 1,
            hasNext = false,
        )
}
