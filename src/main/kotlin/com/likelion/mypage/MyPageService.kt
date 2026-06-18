package com.likelion.mypage

import com.likelion.domain.user.UserType
import com.likelion.store.PageResponse
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MyPageService {
    fun getMyPage(): MyPageResponse =
        MyPageResponse(
            userId = 1024,
            email = "student@kw.ac.kr",
            name = "홍길동",
            userType = UserType.STUDENT,
            college = MyCollegeResponse(collegeId = 1, name = "소프트웨어융합대학"),
            department = MyDepartmentResponse(departmentId = 3, name = "컴퓨터정보공학부"),
            favoriteCount = 3,
            benefitUsageCount = 5,
        )

    fun getMyBenefitUsages(page: Int, size: Int): PageResponse<BenefitUsageResponse> =
        PageResponse(
            content = listOf(
                BenefitUsageResponse(
                    usageId = 1,
                    storeId = 1,
                    storeName = "윤스쿡",
                    benefitTitle = "광운대생 10% 할인",
                    usedAt = LocalDateTime.now(),
                ),
            ),
            page = page,
            size = size,
            totalElements = 1,
            totalPages = 1,
            hasNext = false,
        )
}
