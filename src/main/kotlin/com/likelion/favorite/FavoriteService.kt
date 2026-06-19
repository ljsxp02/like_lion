package com.likelion.favorite

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.common.pageRequestOf
import com.likelion.domain.benefit.BenefitTitleResolver
import com.likelion.domain.favorite.FavoriteEntity
import com.likelion.domain.favorite.FavoriteRepository
import com.likelion.domain.store.StoreEntity
import com.likelion.domain.store.StoreRepository
import com.likelion.store.PageResponse
import com.likelion.store.StoreListItemResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoriteService(
    private val currentUserProvider: CurrentUserProvider,
    private val favoriteRepository: FavoriteRepository,
    private val storeRepository: StoreRepository,
    private val benefitTitleResolver: BenefitTitleResolver,
) {
    @Transactional
    fun addFavorite(storeId: Long): FavoriteResultResponse {
        val userId = currentUserProvider.currentUserId()
        storeRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(storeId)
            ?: throw ApiException(ErrorCode.STORE_404)

        if (favoriteRepository.existsByUserIdAndStoreId(userId, storeId)) {
            throw ApiException(ErrorCode.FAVORITE_409)
        }

        favoriteRepository.save(FavoriteEntity(userId = userId, storeId = storeId))

        return FavoriteResultResponse(storeId = storeId, isFavorite = true)
    }

    @Transactional
    fun removeFavorite(storeId: Long): FavoriteResultResponse {
        val userId = currentUserProvider.currentUserId()
        if (!storeRepository.existsById(storeId)) {
            throw ApiException(ErrorCode.STORE_404)
        }

        favoriteRepository.findByUserIdAndStoreId(userId, storeId)
            ?.let { favoriteRepository.delete(it) }

        return FavoriteResultResponse(storeId = storeId, isFavorite = false)
    }

    @Transactional(readOnly = true)
    fun getMyFavorites(page: Int, size: Int): PageResponse<StoreListItemResponse> {
        val storePage = favoriteRepository.findVisibleFavoriteStoresByUserId(
            userId = currentUserProvider.currentUserId(),
            pageable = pageRequestOf(page, size),
        )
        val benefitTitles = benefitTitleResolver.titlesByStoreId(storePage.content.mapNotNull { it.id })

        return PageResponse(
            content = storePage.content.map { it.toFavoriteListItem(benefitTitles) },
            page = storePage.number,
            size = storePage.size,
            totalElements = storePage.totalElements,
            totalPages = storePage.totalPages,
            hasNext = storePage.hasNext(),
        )
    }

    private fun StoreEntity.toFavoriteListItem(benefitTitles: Map<Long, String>): StoreListItemResponse {
        val storeId = requireNotNull(id)
        return StoreListItemResponse(
            storeId = storeId,
            name = name,
            thumbnailUrl = thumbnailUrl,
            address = address,
            description = benefitTitleResolver.titleOrFallback(benefitTitles, storeId),
            isFavorite = true,
        )
    }
}
