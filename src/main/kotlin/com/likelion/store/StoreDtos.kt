package com.likelion.store

data class StoreSearchCondition(
    val collegeId: Long?,
    val departmentId: Long?,
    val keyword: String?,
    val favoriteOnly: Boolean?,
    val page: Int,
    val size: Int,
)

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class StoreListItemResponse(
    val storeId: Long,
    val name: String,
    val thumbnailUrl: String?,
    val address: String,
    val description: String,
    val isFavorite: Boolean,
)

data class MapStoreResponse(
    val storeId: Long,
    val name: String,
    val thumbnailUrl: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val categories: List<String>,
)

data class MapStoresResponse(
    val stores: List<MapStoreResponse>,
)

data class StoreSummaryResponse(
    val storeId: Long,
    val name: String,
    val thumbnailUrl: String?,
    val address: String,
    val description: String,
    val isFavorite: Boolean,
)

data class StoreDetailResponse(
    val storeId: Long,
    val name: String,
    val address: String,
    val location: String?,
    val contact: String?,
    val thumbnailUrl: String?,
    val description: String,
    val isSchoolWide: Boolean,
    val benefits: List<BenefitResponse>,
    val menus: List<MenuResponse>,
    val isFavorite: Boolean,
)

data class MenuResponse(
    val menuId: Long,
    val name: String,
    val imageUrl: String?,
    val isRepresentative: Boolean,
    val displayOrder: Int,
)

data class BenefitResponse(
    val benefitId: Long,
    val title: String,
    val description: String,
    val startDate: java.time.LocalDate,
    val endDate: java.time.LocalDate,
    val isActive: Boolean,
)

data class StoreAutocompleteResponse(
    val storeId: Long,
    val name: String,
    val address: String,
)

data class StoreAutocompleteListResponse(
    val suggestions: List<StoreAutocompleteResponse>,
)
