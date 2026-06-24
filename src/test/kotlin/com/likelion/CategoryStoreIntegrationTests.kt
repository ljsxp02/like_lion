package com.likelion

import com.likelion.category.CategoryService
import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.domain.benefit.BenefitEntity
import com.likelion.domain.benefit.BenefitRepository
import com.likelion.domain.category.CollegeEntity
import com.likelion.domain.category.CollegeRepository
import com.likelion.domain.category.DepartmentEntity
import com.likelion.domain.category.DepartmentRepository
import com.likelion.domain.menu.MenuEntity
import com.likelion.domain.menu.MenuRepository
import com.likelion.domain.store.StoreEntity
import com.likelion.domain.store.StoreRepository
import com.likelion.store.StoreSearchCondition
import com.likelion.store.StoreService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@Transactional
class CategoryStoreIntegrationTests @Autowired constructor(
    private val categoryService: CategoryService,
    private val storeService: StoreService,
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository,
    private val benefitRepository: BenefitRepository,
) {
    @Test
    fun `단과대와 학과는 DB 데이터를 id 순으로 조회한다`() {
        val firstCollege = collegeRepository.save(CollegeEntity(name = "소프트웨어융합대학"))
        collegeRepository.save(CollegeEntity(name = "전자정보공과대학"))
        departmentRepository.save(
            DepartmentEntity(collegeId = requireNotNull(firstCollege.id), name = "컴퓨터정보공학부"),
        )

        val colleges = categoryService.getColleges()
        val departments = categoryService.getDepartments(requireNotNull(firstCollege.id))

        assertEquals(listOf("소프트웨어융합대학", "전자정보공과대학"), colleges.colleges.map { it.name })
        assertEquals(listOf("컴퓨터정보공학부"), departments.departments.map { it.name })
    }

    @Test
    fun `존재하지 않는 단과대 학과 조회는 COMMON_400이다`() {
        val exception = assertFailsWith<ApiException> {
            categoryService.getDepartments(Long.MAX_VALUE)
        }

        assertEquals(ErrorCode.COMMON_400, exception.errorCode)
    }

    @Test
    fun `매장 목록은 활성 미삭제 매장과 현재 혜택을 페이지네이션한다`() {
        val visibleStore = createStore("윤스쿡")
        createBenefit(visibleStore, "광운대생 10% 할인")
        createStore("비활성 매장", isActive = false)
        createStore("삭제 매장", deletedAt = LocalDateTime.now())

        val response = storeService.getStores(
            StoreSearchCondition(
                collegeId = null,
                departmentId = null,
                keyword = "윤스",
                favoriteOnly = false,
                page = 0,
                size = 20,
            ),
        )

        assertEquals(1, response.totalElements)
        assertEquals("윤스쿡", response.content.single().name)
        assertEquals("광운대생 10% 할인", response.content.single().description)
        assertFalse(response.content.single().isFavorite)
    }

    @Test
    fun `지도 조회는 좌표가 있는 매장만 반경 내에서 반환한다`() {
        createStore("가까운 매장", latitude = 37.6198, longitude = 127.0591)
        createStore("먼 매장", latitude = 37.70, longitude = 127.10)
        createStore("좌표 없는 매장")

        val response = storeService.getMapStores(
            latitude = 37.6198,
            longitude = 127.0591,
            radiusMeters = 1_000,
            collegeId = null,
            departmentId = null,
        )

        assertEquals(listOf("가까운 매장"), response.stores.map { it.name })
        assertTrue(response.stores.single().categories.isEmpty())
    }

    @Test
    fun `지도 좌표 일부만 전달하면 COMMON_400이다`() {
        val exception = assertFailsWith<ApiException> {
            storeService.getMapStores(37.6198, null, 1_000, null, null)
        }

        assertEquals(ErrorCode.COMMON_400, exception.errorCode)
    }

    @Test
    fun `매장 상세는 현재 활성 혜택과 메뉴 순서를 반환한다`() {
        val store = createStore("윤스쿡")
        createBenefit(store, "현재 혜택")
        benefitRepository.save(
            BenefitEntity(
                storeId = requireNotNull(store.id),
                title = "만료 혜택",
                description = "만료됨",
                isSchoolWide = true,
                startDate = LocalDate.now().minusDays(10),
                endDate = LocalDate.now().minusDays(1),
            ),
        )
        menuRepository.save(
            MenuEntity(
                storeId = requireNotNull(store.id),
                name = "두 번째 메뉴",
                isRepresentative = false,
                displayOrder = 2,
            ),
        )
        menuRepository.save(
            MenuEntity(
                storeId = requireNotNull(store.id),
                name = "첫 번째 메뉴",
                isRepresentative = true,
                displayOrder = 1,
            ),
        )

        val response = storeService.getStoreDetail(requireNotNull(store.id))

        assertEquals(listOf("현재 혜택"), response.benefits.map { it.title })
        assertEquals(listOf("첫 번째 메뉴", "두 번째 메뉴"), response.menus.map { it.name })
        assertTrue(response.isSchoolWide)
    }

    private fun createStore(
        name: String,
        isActive: Boolean = true,
        deletedAt: LocalDateTime? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): StoreEntity =
        storeRepository.save(
            StoreEntity(
                name = name,
                address = "서울 노원구 광운로 20",
                latitude = latitude,
                longitude = longitude,
                isActive = isActive,
                deletedAt = deletedAt,
            ),
        )

    private fun createBenefit(store: StoreEntity, title: String): BenefitEntity =
        benefitRepository.save(
            BenefitEntity(
                storeId = requireNotNull(store.id),
                title = title,
                description = "$title 상세",
                isSchoolWide = true,
                startDate = LocalDate.now().minusDays(1),
                endDate = LocalDate.now().plusDays(1),
            ),
        )
}
