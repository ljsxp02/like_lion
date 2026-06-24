package com.likelion.store

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.common.pageRequestOf
import com.likelion.domain.benefit.BenefitEntity
import com.likelion.domain.benefit.BenefitRepository
import com.likelion.domain.benefit.BenefitTitleResolver
import com.likelion.domain.category.CollegeRepository
import com.likelion.domain.category.DepartmentRepository
import com.likelion.domain.favorite.FavoriteRepository
import com.likelion.domain.menu.MenuRepository
import com.likelion.domain.store.StoreEntity
import com.likelion.domain.store.StoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class StoreService(
    private val storeRepository: StoreRepository,
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
    private val menuRepository: MenuRepository,
    private val benefitRepository: BenefitRepository,
    private val favoriteRepository: FavoriteRepository,
    private val benefitTitleResolver: BenefitTitleResolver,
    private val currentUserProvider: CurrentUserProvider,
) {
    @Transactional(readOnly = true)
    fun getStores(condition: StoreSearchCondition): PageResponse<StoreListItemResponse> {
        validateCategoryFilters(condition.collegeId, condition.departmentId)
        val keyword = condition.keyword?.trim()?.takeIf { it.isNotEmpty() }
        val userId = currentUserProvider.currentUserIdOrNull()
        if (condition.favoriteOnly == true && userId == null) {
            throw ApiException(ErrorCode.AUTH_001)
        }
        val storePage = storeRepository.searchVisibleStores(
            collegeId = condition.collegeId,
            departmentId = condition.departmentId,
            keyword = keyword,
            favoriteOnly = condition.favoriteOnly == true,
            userId = userId ?: ANONYMOUS_USER_ID,
            pageable = pageRequestOf(condition.page, condition.size),
        )
        val storeIds = storePage.content.mapNotNull { it.id }
        val benefitTitles = benefitTitleResolver.titlesByStoreId(storeIds)
        val favoriteStoreIds = favoriteStoreIds(userId, storeIds)

        return PageResponse(
            content = storePage.content.map {
                it.toListItem(
                    description = benefitTitleResolver.titleOrFallback(benefitTitles, requireNotNull(it.id)),
                    isFavorite = requireNotNull(it.id) in favoriteStoreIds,
                )
            },
            page = storePage.number,
            size = storePage.size,
            totalElements = storePage.totalElements,
            totalPages = storePage.totalPages,
            hasNext = storePage.hasNext(),
        )
    }

    @Transactional(readOnly = true)
    fun getMapStores(
        latitude: Double?,
        longitude: Double?,
        radiusMeters: Int?,
        collegeId: Long?,
        departmentId: Long?,
    ): MapStoresResponse {
        validateMapParameters(latitude, longitude, radiusMeters)
        validateCategoryFilters(collegeId, departmentId)

        val stores = storeRepository.findVisibleStoresForMap(collegeId, departmentId)
            .asSequence()
            .filter { it.latitude != null && it.longitude != null }
            .filter {
                if (latitude == null || longitude == null || radiusMeters == null) {
                    true
                } else {
                    distanceMeters(latitude, longitude, requireNotNull(it.latitude), requireNotNull(it.longitude)) <= radiusMeters
                }
            }
            .map {
                MapStoreResponse(
                    storeId = requireNotNull(it.id),
                    name = it.name,
                    thumbnailUrl = it.thumbnailUrl,
                    address = it.address,
                    latitude = requireNotNull(it.latitude),
                    longitude = requireNotNull(it.longitude),
                    categories = emptyList(),
                )
            }
            .toList()

        return MapStoresResponse(stores = stores)
    }

    @Transactional(readOnly = true)
    fun searchStores(keyword: String, limit: Int): StoreAutocompleteListResponse {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isEmpty() || limit <= 0) {
            throw ApiException(ErrorCode.COMMON_400)
        }

        return StoreAutocompleteListResponse(
            suggestions = storeRepository
                .findByNameContainingIgnoreCaseAndIsActiveTrueAndDeletedAtIsNullOrderByIdAsc(
                    normalizedKeyword,
                    pageRequestOf(0, limit),
                )
                .map {
                    StoreAutocompleteResponse(
                        storeId = requireNotNull(it.id),
                        name = it.name,
                        address = it.address,
                    )
                },
        )
    }

    @Transactional(readOnly = true)
    fun getStoreSummary(storeId: Long): StoreSummaryResponse {
        val store = findVisibleStore(storeId)
        val userId = currentUserProvider.currentUserIdOrNull()
        val benefitTitle = benefitTitleResolver.titlesByStoreId(listOf(storeId))[storeId]

        return StoreSummaryResponse(
            storeId = storeId,
            name = store.name,
            thumbnailUrl = store.thumbnailUrl,
            address = store.address,
            description = benefitTitle ?: "혜택 정보 없음",
            isFavorite = userId?.let { favoriteRepository.existsByUserIdAndStoreId(it, storeId) } ?: false,
        )
    }

    @Transactional(readOnly = true)
    fun getStoreDetail(storeId: Long): StoreDetailResponse {
        val store = findVisibleStore(storeId)
        val benefits = benefitRepository.findCurrentBenefitsByStoreId(storeId, LocalDate.now())
        val menus = menuRepository.findAllByStoreIdOrderByDisplayOrderAsc(storeId)

        return StoreDetailResponse(
            storeId = storeId,
            name = store.name,
            address = store.address,
            location = store.location,
            contact = store.contact,
            thumbnailUrl = store.thumbnailUrl,
            description = benefits.firstOrNull()?.title ?: "혜택 정보 없음",
            isSchoolWide = benefits.any(BenefitEntity::isSchoolWide),
            benefits = benefits.map {
                BenefitResponse(
                    benefitId = requireNotNull(it.id),
                    title = it.title,
                    description = it.description,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    isActive = true,
                )
            },
            menus = menus.map {
                MenuResponse(
                    menuId = requireNotNull(it.id),
                    name = it.name,
                    imageUrl = it.imageUrl,
                    isRepresentative = it.isRepresentative,
                    displayOrder = it.displayOrder,
                )
            },
            isFavorite = currentUserProvider.currentUserIdOrNull()
                ?.let { favoriteRepository.existsByUserIdAndStoreId(it, storeId) }
                ?: false,
        )
    }

    private fun validateCategoryFilters(collegeId: Long?, departmentId: Long?) {
        if (collegeId != null && !collegeRepository.existsById(collegeId)) {
            throw ApiException(ErrorCode.COMMON_400)
        }
        if (departmentId != null) {
            val department = departmentRepository.findById(departmentId)
                .orElseThrow { ApiException(ErrorCode.COMMON_400) }
            if (collegeId != null && department.collegeId != collegeId) {
                throw ApiException(ErrorCode.COMMON_400)
            }
        }
    }

    private fun validateMapParameters(latitude: Double?, longitude: Double?, radiusMeters: Int?) {
        val anyProvided = latitude != null || longitude != null || radiusMeters != null
        val allProvided = latitude != null && longitude != null && radiusMeters != null
        if (anyProvided && !allProvided) {
            throw ApiException(ErrorCode.COMMON_400)
        }
        if (!allProvided) {
            return
        }

        if (
            requireNotNull(latitude) !in -90.0..90.0 ||
            requireNotNull(longitude) !in -180.0..180.0 ||
            requireNotNull(radiusMeters) <= 0
        ) {
            throw ApiException(ErrorCode.COMMON_400)
        }
    }

    private fun findVisibleStore(storeId: Long): StoreEntity =
        storeRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(storeId)
            ?: throw ApiException(ErrorCode.STORE_404)

    private fun favoriteStoreIds(userId: Long?, storeIds: List<Long>): Set<Long> =
        if (userId == null || storeIds.isEmpty()) {
            emptySet()
        } else {
            favoriteRepository.findFavoriteStoreIds(userId, storeIds)
        }

    private fun StoreEntity.toListItem(description: String, isFavorite: Boolean): StoreListItemResponse =
        StoreListItemResponse(
            storeId = requireNotNull(id),
            name = name,
            thumbnailUrl = thumbnailUrl,
            address = address,
            description = description,
            isFavorite = isFavorite,
        )

    private fun distanceMeters(
        originLatitude: Double,
        originLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double,
    ): Double {
        val latitudeDistance = Math.toRadians(targetLatitude - originLatitude)
        val longitudeDistance = Math.toRadians(targetLongitude - originLongitude)
        val a = sin(latitudeDistance / 2).pow(2) +
            cos(Math.toRadians(originLatitude)) *
            cos(Math.toRadians(targetLatitude)) *
            sin(longitudeDistance / 2).pow(2)
        return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
    }

    private companion object {
        const val EARTH_RADIUS_METERS = 6_371_000.0
        const val ANONYMOUS_USER_ID = -1L
    }
}
