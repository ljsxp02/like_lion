package com.likelion.mypage

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.common.pageRequestOf
import com.likelion.domain.benefit.BenefitTitleResolver
import com.likelion.domain.benefitusage.BenefitUsageEntity
import com.likelion.domain.benefitusage.BenefitUsageRepository
import com.likelion.domain.category.CollegeRepository
import com.likelion.domain.category.DepartmentRepository
import com.likelion.domain.favorite.FavoriteRepository
import com.likelion.domain.store.StoreRepository
import com.likelion.domain.user.UserEntity
import com.likelion.domain.user.UserRepository
import com.likelion.store.PageResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MyPageService(
    private val currentUserProvider: CurrentUserProvider,
    private val userRepository: UserRepository,
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val benefitUsageRepository: BenefitUsageRepository,
    private val storeRepository: StoreRepository,
    private val benefitTitleResolver: BenefitTitleResolver,
) {
    @Transactional(readOnly = true)
    fun getMyPage(): MyPageResponse {
        val userId = currentUserProvider.currentUserId()
        val user = userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCode.USER_404) }

        return MyPageResponse(
            userId = userId,
            email = user.email,
            name = user.name,
            userType = user.userType,
            college = user.toCollegeResponse(),
            department = user.toDepartmentResponse(),
            favoriteCount = favoriteRepository.countVisibleFavoriteStoresByUserId(userId).toInt(),
            benefitUsageCount = benefitUsageRepository.countByUserId(userId).toInt(),
        )
    }

    @Transactional(readOnly = true)
    fun getMyBenefitUsages(page: Int, size: Int): PageResponse<BenefitUsageResponse> {
        val usagePage = benefitUsageRepository.findAllByUserIdOrderByUsedAtDesc(
            userId = currentUserProvider.currentUserId(),
            pageable = pageRequestOf(page, size),
        )
        val storeIds = usagePage.content.map { it.storeId }
        val storeNames = storeRepository.findAllById(storeIds.toSet())
            .associate { requireNotNull(it.id) to it.name }
        val benefitTitles = benefitTitleResolver.titlesByStoreId(storeIds)

        return PageResponse(
            content = usagePage.content.map { it.toResponse(storeNames, benefitTitles) },
            page = usagePage.number,
            size = usagePage.size,
            totalElements = usagePage.totalElements,
            totalPages = usagePage.totalPages,
            hasNext = usagePage.hasNext(),
        )
    }

    private fun UserEntity.toCollegeResponse(): MyCollegeResponse? =
        collegeId
            ?.let { collegeRepository.findById(it).orElse(null) }
            ?.let { MyCollegeResponse(collegeId = requireNotNull(it.id), name = it.name) }

    private fun UserEntity.toDepartmentResponse(): MyDepartmentResponse? =
        departmentId
            ?.let { departmentRepository.findById(it).orElse(null) }
            ?.let { MyDepartmentResponse(departmentId = requireNotNull(it.id), name = it.name) }

    private fun BenefitUsageEntity.toResponse(
        storeNames: Map<Long, String>,
        benefitTitles: Map<Long, String>,
    ): BenefitUsageResponse =
        BenefitUsageResponse(
            usageId = requireNotNull(id),
            storeId = storeId,
            storeName = storeNames[storeId] ?: STORE_NAME_FALLBACK,
            benefitTitle = benefitTitleResolver.titleOrFallback(benefitTitles, storeId),
            usedAt = usedAt,
        )

    companion object {
        private const val STORE_NAME_FALLBACK = "알 수 없는 매장"
    }
}
