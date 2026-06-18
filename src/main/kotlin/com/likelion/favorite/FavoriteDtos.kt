package com.likelion.favorite

import com.likelion.store.PageResponse
import com.likelion.store.StoreListItemResponse

data class FavoriteResultResponse(
    val storeId: Long,
    val isFavorite: Boolean,
)

typealias FavoritePageResponse = PageResponse<StoreListItemResponse>
