package com.likelion

import com.likelion.admin.AdminBenefitCreateRequest
import com.likelion.admin.AdminBenefitUpdateRequest
import com.likelion.admin.AdminMenuCreateRequest
import com.likelion.admin.AdminMenuUpdateRequest
import com.likelion.admin.AdminService
import com.likelion.admin.AdminStoreCreateRequest
import com.likelion.admin.AdminStoreUpdateRequest
import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUser
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.domain.benefit.BenefitRepository
import com.likelion.domain.benefitusage.BenefitUsageRepository
import com.likelion.domain.menu.MenuRepository
import com.likelion.domain.qr.QrTokenRepository
import com.likelion.domain.store.StoreRepository
import com.likelion.domain.user.UserEntity
import com.likelion.domain.user.UserRepository
import com.likelion.domain.user.UserType
import com.likelion.qr.QrService
import com.likelion.qr.QrVerifyRequest
import com.likelion.store.StoreSearchCondition
import com.likelion.store.StoreService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

@SpringBootTest
@Transactional
@Import(AdminQrIntegrationTests.TestCurrentUserConfig::class)
class AdminQrIntegrationTests @Autowired constructor(
    private val adminService: AdminService,
    private val qrService: QrService,
    private val storeService: StoreService,
    private val currentUserProvider: AdminQrTestCurrentUserProvider,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository,
    private val benefitRepository: BenefitRepository,
    private val qrTokenRepository: QrTokenRepository,
    private val benefitUsageRepository: BenefitUsageRepository,
) {
    @Test
    fun `관리자는 매장을 등록 수정 비활성화할 수 있다`() {
        currentUserProvider.asAdmin()

        val created = adminService.createStore(
            AdminStoreCreateRequest(
                name = "관리자 등록 매장",
                address = "서울 노원구 광운로 20",
                location = "광운대 정문",
                contact = "02-0000-0000",
                thumbnailUrl = "https://example.com/store.jpg",
                latitude = 37.6198,
                longitude = 127.0591,
            ),
        )

        assertNotNull(storeRepository.findById(created.storeId).orElse(null))
        assertEquals(1, findStores("관리자 등록").totalElements)

        val updated = adminService.updateStore(
            created.storeId,
            AdminStoreUpdateRequest(
                name = "수정된 매장",
                address = null,
                location = null,
                contact = null,
                thumbnailUrl = null,
                latitude = null,
                longitude = null,
            ),
        )

        assertEquals("수정된 매장", updated.name)

        val deactivated = adminService.deactivateStore(created.storeId)

        assertFalse(deactivated.isActive)
        assertEquals(0, findStores("수정된 매장").totalElements)
    }

    @Test
    fun `관리자가 아니면 관리자 API 서비스를 사용할 수 없다`() {
        currentUserProvider.asStudent(userId = 1)

        val exception = assertFailsWith<ApiException> {
            adminService.createStore(
                AdminStoreCreateRequest(
                    name = "권한 없는 등록",
                    address = "서울 노원구 광운로 20",
                    location = null,
                    contact = null,
                    thumbnailUrl = null,
                    latitude = null,
                    longitude = null,
                ),
            )
        }

        assertEquals(ErrorCode.AUTH_004, exception.errorCode)
    }

    @Test
    fun `관리자는 메뉴와 혜택을 생성하고 수정할 수 있다`() {
        currentUserProvider.asAdmin()
        val storeId = createStore("메뉴 혜택 매장")

        val menu = adminService.createMenu(
            storeId,
            AdminMenuCreateRequest(
                name = "아메리카노",
                imageUrl = "https://example.com/americano.jpg",
                isRepresentative = true,
                displayOrder = 1,
            ),
        )
        val updatedMenu = adminService.updateMenu(
            menu.menuId,
            AdminMenuUpdateRequest(
                name = "카페라떼",
                imageUrl = null,
                isRepresentative = false,
                displayOrder = 2,
            ),
        )

        assertEquals("카페라떼", updatedMenu.name)
        assertFalse(updatedMenu.isRepresentative)
        assertEquals(2, updatedMenu.displayOrder)
        assertEquals("카페라떼", menuRepository.findById(menu.menuId).orElseThrow().name)

        val benefit = adminService.createBenefit(
            storeId,
            AdminBenefitCreateRequest(
                title = "광운대생 10% 할인",
                description = "학생 인증 시 할인",
                isSchoolWide = true,
                collegeIds = null,
                departmentIds = null,
                startDate = LocalDate.now().minusDays(1),
                endDate = LocalDate.now().plusDays(30),
            ),
        )
        val updatedBenefit = adminService.updateBenefit(
            benefit.benefitId,
            AdminBenefitUpdateRequest(
                title = "광운대생 15% 할인",
                description = null,
                isSchoolWide = null,
                collegeIds = null,
                departmentIds = null,
                startDate = null,
                endDate = null,
                isActive = true,
            ),
        )

        assertEquals("광운대생 15% 할인", updatedBenefit.title)
        assertEquals("광운대생 15% 할인", benefitRepository.findById(benefit.benefitId).orElseThrow().title)
    }

    @Test
    fun `QR 재발급은 기존 활성 토큰을 무효화하고 새 토큰을 만든다`() {
        currentUserProvider.asAdmin()
        val storeId = createStore("QR 매장")
        val first = adminService.regenerateQr(storeId)
        val second = adminService.regenerateQr(storeId)

        assertNotEquals(first.qrToken, second.qrToken)
        assertEquals(null, qrTokenRepository.findByTokenAndIsActiveTrue(first.qrToken))
        assertNotNull(qrTokenRepository.findByTokenAndIsActiveTrue(second.qrToken))
    }

    @Test
    fun `QR 인증은 혜택 사용 내역을 저장하고 같은 사용자의 중복 인증을 막는다`() {
        currentUserProvider.asAdmin()
        val storeId = createStore("QR 인증 매장")
        val qrToken = adminService.regenerateQr(storeId).qrToken
        val student = createStudent()
        currentUserProvider.asStudent(requireNotNull(student.id))

        val response = qrService.verify(QrVerifyRequest(qrToken = qrToken))

        assertEquals(storeId, response.storeId)
        assertEquals("QR 인증 매장", response.storeName)
        val activeToken = requireNotNull(qrTokenRepository.findByTokenAndIsActiveTrue(qrToken))
        assertTrue(
            benefitUsageRepository.existsByUserIdAndQrTokenId(
                requireNotNull(student.id),
                requireNotNull(activeToken.id),
            ),
        )
        assertEquals(1, benefitUsageRepository.countByUserId(requireNotNull(student.id)))

        val exception = assertFailsWith<ApiException> {
            qrService.verify(QrVerifyRequest(qrToken = qrToken))
        }

        assertEquals(ErrorCode.QR_409, exception.errorCode)
    }

    @Test
    fun `존재하지 않는 관리자 대상은 명확한 에러를 반환한다`() {
        currentUserProvider.asAdmin()

        assertEquals(
            ErrorCode.ADMIN_STORE_404,
            assertFailsWith<ApiException> { adminService.updateStore(Long.MAX_VALUE, emptyStoreUpdate()) }.errorCode,
        )
        assertEquals(
            ErrorCode.ADMIN_MENU_404,
            assertFailsWith<ApiException> { adminService.updateMenu(Long.MAX_VALUE, AdminMenuUpdateRequest(null, null, null, null)) }.errorCode,
        )
        assertEquals(
            ErrorCode.ADMIN_BENEFIT_404,
            assertFailsWith<ApiException> { adminService.updateBenefit(Long.MAX_VALUE, emptyBenefitUpdate()) }.errorCode,
        )
    }

    private fun createStore(name: String): Long =
        adminService.createStore(
            AdminStoreCreateRequest(
                name = name,
                address = "서울 노원구 광운로 20",
                location = null,
                contact = null,
                thumbnailUrl = null,
                latitude = 37.6198,
                longitude = 127.0591,
            ),
        ).storeId

    private fun createStudent(): UserEntity =
        userRepository.save(
            UserEntity(
                email = "qr-student-${System.nanoTime()}@kw.ac.kr",
                passwordHash = "hashed-password",
                name = "QR 학생",
                userType = UserType.STUDENT,
                isEmailVerified = true,
            ),
        )

    private fun findStores(keyword: String) =
        storeService.getStores(
            StoreSearchCondition(
                collegeId = null,
                departmentId = null,
                keyword = keyword,
                favoriteOnly = false,
                page = 0,
                size = 20,
            ),
        )

    private fun emptyStoreUpdate(): AdminStoreUpdateRequest =
        AdminStoreUpdateRequest(
            name = null,
            address = null,
            location = null,
            contact = null,
            thumbnailUrl = null,
            latitude = null,
            longitude = null,
        )

    private fun emptyBenefitUpdate(): AdminBenefitUpdateRequest =
        AdminBenefitUpdateRequest(
            title = null,
            description = null,
            isSchoolWide = null,
            collegeIds = null,
            departmentIds = null,
            startDate = null,
            endDate = null,
            isActive = null,
        )

    @TestConfiguration
    class TestCurrentUserConfig {
        @Bean
        @Primary
        fun adminQrCurrentUserProvider(): AdminQrTestCurrentUserProvider = AdminQrTestCurrentUserProvider()
    }
}

class AdminQrTestCurrentUserProvider : CurrentUserProvider {
    var userId: Long = -1L
    var email: String = "admin@kw.ac.kr"
    var userType: String = UserType.ADMIN.name

    fun asAdmin(userId: Long = -1L) {
        this.userId = userId
        email = "admin@kw.ac.kr"
        userType = UserType.ADMIN.name
    }

    fun asStudent(userId: Long) {
        this.userId = userId
        email = "student@kw.ac.kr"
        userType = UserType.STUDENT.name
    }

    override fun currentUser(): CurrentUser =
        CurrentUser(
            userId = userId,
            email = email,
            userType = userType,
        )
}
