package com.likelion

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.common.auth.CurrentUser
import com.likelion.common.auth.CurrentUserProvider
import com.likelion.domain.benefit.BenefitEntity
import com.likelion.domain.benefit.BenefitRepository
import com.likelion.domain.benefitusage.BenefitUsageEntity
import com.likelion.domain.benefitusage.BenefitUsageRepository
import com.likelion.domain.category.CollegeEntity
import com.likelion.domain.category.CollegeRepository
import com.likelion.domain.category.DepartmentEntity
import com.likelion.domain.category.DepartmentRepository
import com.likelion.domain.favorite.FavoriteEntity
import com.likelion.domain.favorite.FavoriteRepository
import com.likelion.domain.qr.QrTokenEntity
import com.likelion.domain.qr.QrTokenRepository
import com.likelion.domain.store.StoreEntity
import com.likelion.domain.store.StoreRepository
import com.likelion.domain.user.UserEntity
import com.likelion.domain.user.UserRepository
import com.likelion.domain.user.UserType
import com.likelion.favorite.FavoriteService
import com.likelion.mypage.MyPageService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

@SpringBootTest
@Transactional
@Import(FavoriteMyPageIntegrationTests.TestCurrentUserConfig::class)
class FavoriteMyPageIntegrationTests @Autowired constructor(
    private val favoriteService: FavoriteService,
    private val myPageService: MyPageService,
    private val currentUserProvider: MutableTestCurrentUserProvider,
    private val userRepository: UserRepository,
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
    private val storeRepository: StoreRepository,
    private val favoriteRepository: FavoriteRepository,
    private val benefitRepository: BenefitRepository,
    private val qrTokenRepository: QrTokenRepository,
    private val benefitUsageRepository: BenefitUsageRepository,
) {
    @Test
    fun `즐겨찾기 추가는 현재 사용자 기준으로 저장한다`() {
        val user = createCurrentUser()
        val store = createStore(name = "윤스쿡")

        val response = favoriteService.addFavorite(requireNotNull(store.id))

        assertEquals(store.id, response.storeId)
        assertTrue(response.isFavorite)
        assertTrue(favoriteRepository.existsByUserIdAndStoreId(requireNotNull(user.id), requireNotNull(store.id)))
    }

    @Test
    fun `이미 즐겨찾기한 매장은 FAVORITE_409를 반환한다`() {
        val user = createCurrentUser()
        val store = createStore(name = "윤스쿡")
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(store.id)))

        val exception = assertFailsWith<ApiException> {
            favoriteService.addFavorite(requireNotNull(store.id))
        }

        assertEquals(ErrorCode.FAVORITE_409, exception.errorCode)
    }

    @Test
    fun `비활성 또는 삭제 매장은 즐겨찾기 추가 대상에서 제외한다`() {
        createCurrentUser()
        val inactiveStore = createStore(name = "비활성 매장", isActive = false)
        val deletedStore = createStore(name = "삭제 매장", deletedAt = LocalDateTime.now())

        val inactiveException = assertFailsWith<ApiException> {
            favoriteService.addFavorite(requireNotNull(inactiveStore.id))
        }
        val deletedException = assertFailsWith<ApiException> {
            favoriteService.addFavorite(requireNotNull(deletedStore.id))
        }

        assertEquals(ErrorCode.STORE_404, inactiveException.errorCode)
        assertEquals(ErrorCode.STORE_404, deletedException.errorCode)
    }

    @Test
    fun `즐겨찾기가 없어도 해제는 멱등하게 성공한다`() {
        val user = createCurrentUser()
        val store = createStore(name = "윤스쿡")

        val response = favoriteService.removeFavorite(requireNotNull(store.id))

        assertFalse(response.isFavorite)
        assertFalse(favoriteRepository.existsByUserIdAndStoreId(requireNotNull(user.id), requireNotNull(store.id)))
    }

    @Test
    fun `즐겨찾기가 있으면 해제 시 삭제된다`() {
        val user = createCurrentUser()
        val store = createStore(name = "윤스쿡")
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(store.id)))

        val response = favoriteService.removeFavorite(requireNotNull(store.id))

        assertFalse(response.isFavorite)
        assertFalse(favoriteRepository.existsByUserIdAndStoreId(requireNotNull(user.id), requireNotNull(store.id)))
    }

    @Test
    fun `존재하지 않는 매장 즐겨찾기 해제는 STORE_404를 반환한다`() {
        createCurrentUser()

        val exception = assertFailsWith<ApiException> {
            favoriteService.removeFavorite(Long.MAX_VALUE)
        }

        assertEquals(ErrorCode.STORE_404, exception.errorCode)
    }

    @Test
    fun `내 즐겨찾기 목록은 활성 미삭제 매장만 페이지네이션으로 반환한다`() {
        val user = createCurrentUser()
        val firstStore = createStore(name = "첫 번째 매장")
        val secondStore = createStore(name = "두 번째 매장")
        val inactiveStore = createStore(name = "비활성 매장", isActive = false)
        val deletedStore = createStore(name = "삭제 매장", deletedAt = LocalDateTime.now())
        createBenefit(firstStore, title = "첫 번째 혜택")
        createBenefit(secondStore, title = "두 번째 혜택")

        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(firstStore.id)))
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(secondStore.id)))
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(inactiveStore.id)))
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(deletedStore.id)))

        val firstPage = favoriteService.getMyFavorites(page = 0, size = 1)
        val secondPage = favoriteService.getMyFavorites(page = 1, size = 1)

        assertEquals(2, firstPage.totalElements)
        assertEquals(2, firstPage.totalPages)
        assertTrue(firstPage.hasNext)
        assertEquals("두 번째 매장", firstPage.content.single().name)
        assertEquals("두 번째 혜택", firstPage.content.single().description)
        assertTrue(firstPage.content.single().isFavorite)
        assertEquals("첫 번째 매장", secondPage.content.single().name)
        assertFalse(secondPage.hasNext)
    }

    @Test
    fun `마이페이지는 사용자 정보와 실제 카운트를 반환한다`() {
        val user = createCurrentUser(name = "홍길동")
        val visibleStore = createStore(name = "노출 매장")
        val inactiveStore = createStore(name = "비활성 매장", isActive = false)
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(visibleStore.id)))
        favoriteRepository.save(FavoriteEntity(userId = requireNotNull(user.id), storeId = requireNotNull(inactiveStore.id)))
        createBenefitUsage(user, visibleStore, usedAt = LocalDateTime.now().minusDays(1))
        createBenefitUsage(user, visibleStore, usedAt = LocalDateTime.now())

        val response = myPageService.getMyPage()

        assertEquals(user.id, response.userId)
        assertEquals(user.email, response.email)
        assertEquals("홍길동", response.name)
        assertEquals(UserType.STUDENT, response.userType)
        assertNotNull(response.college)
        assertEquals("소프트웨어융합대학", response.college.name)
        assertNotNull(response.department)
        assertEquals("컴퓨터정보공학부", response.department.name)
        assertEquals(1, response.favoriteCount)
        assertEquals(2, response.benefitUsageCount)
    }

    @Test
    fun `혜택 사용 내역은 최신순과 benefitTitle fallback을 반환한다`() {
        val user = createCurrentUser()
        val titledStore = createStore(name = "혜택 매장")
        val fallbackStore = createStore(name = "혜택 없는 매장")
        createBenefit(titledStore, title = "사용 가능 혜택")
        createBenefitUsage(user, titledStore, usedAt = LocalDateTime.now().minusDays(1))
        createBenefitUsage(user, fallbackStore, usedAt = LocalDateTime.now())

        val firstPage = myPageService.getMyBenefitUsages(page = 0, size = 1)
        val secondPage = myPageService.getMyBenefitUsages(page = 1, size = 1)

        assertEquals(2, firstPage.totalElements)
        assertEquals(2, firstPage.totalPages)
        assertTrue(firstPage.hasNext)
        assertEquals("혜택 없는 매장", firstPage.content.single().storeName)
        assertEquals("혜택 정보 없음", firstPage.content.single().benefitTitle)
        assertEquals("혜택 매장", secondPage.content.single().storeName)
        assertEquals("사용 가능 혜택", secondPage.content.single().benefitTitle)
        assertFalse(secondPage.hasNext)
    }

    private fun createCurrentUser(name: String = "테스트 사용자"): UserEntity {
        val college = collegeRepository.save(CollegeEntity(name = "소프트웨어융합대학"))
        val department = departmentRepository.save(
            DepartmentEntity(collegeId = requireNotNull(college.id), name = "컴퓨터정보공학부"),
        )
        val user = userRepository.save(
            UserEntity(
                email = "student-${System.nanoTime()}@kw.ac.kr",
                passwordHash = "hashed-password",
                name = name,
                userType = UserType.STUDENT,
                isEmailVerified = true,
                collegeId = college.id,
                departmentId = department.id,
            ),
        )
        currentUserProvider.userId = requireNotNull(user.id)
        currentUserProvider.email = user.email
        currentUserProvider.userType = user.userType.name
        return user
    }

    private fun createStore(
        name: String,
        isActive: Boolean = true,
        deletedAt: LocalDateTime? = null,
    ): StoreEntity =
        storeRepository.save(
            StoreEntity(
                name = name,
                address = "서울 노원구 광운로 20",
                thumbnailUrl = "https://image.example.com/$name.jpg",
                isActive = isActive,
                deletedAt = deletedAt,
            ),
        )

    private fun createBenefit(
        store: StoreEntity,
        title: String,
    ): BenefitEntity =
        benefitRepository.save(
            BenefitEntity(
                storeId = requireNotNull(store.id),
                title = title,
                description = "$title 상세",
                isSchoolWide = true,
                startDate = LocalDate.now().minusDays(1),
                endDate = LocalDate.now().plusDays(1),
                isActive = true,
            ),
        )

    private fun createBenefitUsage(
        user: UserEntity,
        store: StoreEntity,
        usedAt: LocalDateTime,
    ): BenefitUsageEntity {
        val qrToken = qrTokenRepository.save(
            QrTokenEntity(
                storeId = requireNotNull(store.id),
                token = "qr-${System.nanoTime()}",
                isActive = true,
                issuedAt = usedAt.minusMinutes(1),
            ),
        )
        return benefitUsageRepository.save(
            BenefitUsageEntity(
                userId = requireNotNull(user.id),
                storeId = requireNotNull(store.id),
                qrTokenId = requireNotNull(qrToken.id),
                usedAt = usedAt,
            ),
        )
    }

    @TestConfiguration
    class TestCurrentUserConfig {
        @Bean
        @Primary
        fun testCurrentUserProvider(): MutableTestCurrentUserProvider = MutableTestCurrentUserProvider()
    }
}

class MutableTestCurrentUserProvider : CurrentUserProvider {
    var userId: Long = -1L
    var email: String = "student@kw.ac.kr"
    var userType: String = UserType.STUDENT.name

    override fun currentUser(): CurrentUser =
        CurrentUser(
            userId = userId,
            email = email,
            userType = userType,
        )
}
