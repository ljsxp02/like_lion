package com.likelion.admin

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.domain.benefit.BenefitEntity
import com.likelion.domain.benefit.BenefitRepository
import com.likelion.domain.category.CollegeEntity
import com.likelion.domain.category.CollegeRepository
import com.likelion.domain.category.DepartmentEntity
import com.likelion.domain.category.DepartmentRepository
import com.likelion.domain.menu.MenuEntity
import com.likelion.domain.menu.MenuRepository
import com.likelion.domain.qr.QrTokenEntity
import com.likelion.domain.qr.QrTokenRepository
import com.likelion.domain.store.StoreEntity
import com.likelion.domain.store.StoreRepository
import com.likelion.domain.user.UserType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class AdminService(
    private val currentUserProvider: CurrentUserProvider,
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository,
    private val benefitRepository: BenefitRepository,
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
    private val qrTokenRepository: QrTokenRepository,
) {
    @Transactional
    fun createStore(request: AdminStoreCreateRequest): AdminStoreResponse {
        requireAdmin()
        val store = storeRepository.save(
            StoreEntity(
                name = request.name,
                address = request.address,
                location = request.location,
                contact = request.contact,
                thumbnailUrl = request.thumbnailUrl,
                latitude = request.latitude,
                longitude = request.longitude,
                isActive = true,
            ),
        )
        return store.toAdminResponse()
    }

    @Transactional
    fun updateStore(storeId: Long, request: AdminStoreUpdateRequest): AdminStoreResponse {
        requireAdmin()
        val store = findStore(storeId)
        request.name?.let { store.name = it }
        request.address?.let { store.address = it }
        request.location?.let { store.location = it }
        request.contact?.let { store.contact = it }
        request.thumbnailUrl?.let { store.thumbnailUrl = it }
        request.latitude?.let { store.latitude = it }
        request.longitude?.let { store.longitude = it }
        return store.toAdminResponse()
    }

    @Transactional
    fun deactivateStore(storeId: Long): AdminStoreDeactivateResponse {
        requireAdmin()
        val store = findStore(storeId)
        store.isActive = false
        store.deletedAt = LocalDateTime.now()
        return AdminStoreDeactivateResponse(storeId = storeId, isActive = store.isActive)
    }

    @Transactional
    fun createMenu(storeId: Long, request: AdminMenuCreateRequest): AdminMenuResponse {
        requireAdmin()
        findStore(storeId)
        val menu = menuRepository.save(
            MenuEntity(
                storeId = storeId,
                name = request.name,
                imageUrl = request.imageUrl,
                isRepresentative = request.isRepresentative,
                displayOrder = request.displayOrder,
            ),
        )
        return menu.toAdminResponse()
    }

    @Transactional
    fun updateMenu(menuId: Long, request: AdminMenuUpdateRequest): AdminMenuResponse {
        requireAdmin()
        val menu = menuRepository.findById(menuId)
            .orElseThrow { ApiException(ErrorCode.ADMIN_MENU_404) }
        request.name?.let { menu.name = it }
        request.imageUrl?.let { menu.imageUrl = it }
        request.isRepresentative?.let { menu.isRepresentative = it }
        request.displayOrder?.let { menu.displayOrder = it }
        return menu.toAdminResponse()
    }

    @Transactional
    fun createBenefit(storeId: Long, request: AdminBenefitCreateRequest): AdminBenefitResponse {
        requireAdmin()
        findStore(storeId)
        validateBenefitPeriod(request.startDate, request.endDate)
        val benefit = benefitRepository.save(
            BenefitEntity(
                storeId = storeId,
                title = request.title,
                description = request.description,
                isSchoolWide = request.isSchoolWide,
                startDate = request.startDate,
                endDate = request.endDate,
                isActive = true,
            ),
        )
        applyTargets(benefit, request.isSchoolWide, request.collegeIds, request.departmentIds)
        return benefit.toAdminResponse()
    }

    @Transactional
    fun updateBenefit(benefitId: Long, request: AdminBenefitUpdateRequest): AdminBenefitResponse {
        requireAdmin()
        val benefit = benefitRepository.findById(benefitId)
            .orElseThrow { ApiException(ErrorCode.ADMIN_BENEFIT_404) }
        request.title?.let { benefit.title = it }
        request.description?.let { benefit.description = it }
        request.isSchoolWide?.let { benefit.isSchoolWide = it }
        request.startDate?.let { benefit.startDate = it }
        request.endDate?.let { benefit.endDate = it }
        request.isActive?.let { benefit.isActive = it }
        validateBenefitPeriod(benefit.startDate, benefit.endDate)
        if (request.isSchoolWide != null || request.collegeIds != null || request.departmentIds != null) {
            applyTargets(benefit, benefit.isSchoolWide, request.collegeIds, request.departmentIds)
        }
        return benefit.toAdminResponse()
    }

    @Transactional
    fun regenerateQr(storeId: Long): QrRegenerateResponse {
        requireAdmin()
        findStore(storeId)
        qrTokenRepository.findAllByStoreIdAndIsActiveTrue(storeId)
            .forEach { it.isActive = false }
        val issuedAt = LocalDateTime.now()
        val qrToken = qrTokenRepository.save(
            QrTokenEntity(
                storeId = storeId,
                token = UUID.randomUUID().toString(),
                isActive = true,
                issuedAt = issuedAt,
            ),
        )
        return QrRegenerateResponse(
            storeId = storeId,
            qrToken = qrToken.token,
            qrTokenIssuedAt = issuedAt,
        )
    }

    private fun requireAdmin() {
        if (currentUserProvider.currentUser().userType != UserType.ADMIN.name) {
            throw ApiException(ErrorCode.AUTH_004)
        }
    }

    private fun findStore(storeId: Long): StoreEntity =
        storeRepository.findById(storeId)
            .filter { it.deletedAt == null }
            .orElseThrow { ApiException(ErrorCode.ADMIN_STORE_404) }

    private fun applyTargets(
        benefit: BenefitEntity,
        isSchoolWide: Boolean,
        collegeIds: List<Long>?,
        departmentIds: List<Long>?,
    ) {
        benefit.targetColleges.clear()
        benefit.targetDepartments.clear()
        if (isSchoolWide) {
            return
        }
        benefit.targetColleges.addAll(findColleges(collegeIds.orEmpty()))
        benefit.targetDepartments.addAll(findDepartments(departmentIds.orEmpty()))
    }

    private fun findColleges(collegeIds: List<Long>): List<CollegeEntity> {
        if (collegeIds.isEmpty()) {
            return emptyList()
        }
        val colleges = collegeRepository.findAllById(collegeIds)
        if (colleges.size != collegeIds.toSet().size) {
            throw ApiException(ErrorCode.COMMON_400)
        }
        return colleges
    }

    private fun findDepartments(departmentIds: List<Long>): List<DepartmentEntity> {
        if (departmentIds.isEmpty()) {
            return emptyList()
        }
        val departments = departmentRepository.findAllById(departmentIds)
        if (departments.size != departmentIds.toSet().size) {
            throw ApiException(ErrorCode.COMMON_400)
        }
        return departments
    }

    private fun validateBenefitPeriod(startDate: LocalDate, endDate: LocalDate) {
        if (endDate.isBefore(startDate)) {
            throw ApiException(ErrorCode.COMMON_400)
        }
    }

    private fun StoreEntity.toAdminResponse(): AdminStoreResponse =
        AdminStoreResponse(
            storeId = requireNotNull(id),
            name = name,
            address = address,
            isActive = isActive,
        )

    private fun MenuEntity.toAdminResponse(): AdminMenuResponse =
        AdminMenuResponse(
            menuId = requireNotNull(id),
            storeId = storeId,
            name = name,
            imageUrl = imageUrl,
            isRepresentative = isRepresentative,
            displayOrder = displayOrder,
        )

    private fun BenefitEntity.toAdminResponse(): AdminBenefitResponse =
        AdminBenefitResponse(
            benefitId = requireNotNull(id),
            storeId = storeId,
            title = title,
            description = description,
            isSchoolWide = isSchoolWide,
            startDate = startDate,
            endDate = endDate,
            isActive = isActive,
        )
}
