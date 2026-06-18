package com.likelion.mypage

import com.likelion.domain.user.UserType
import java.time.LocalDateTime

data class MyPageResponse(
    val userId: Long,
    val email: String,
    val name: String,
    val userType: UserType,
    val college: MyCollegeResponse?,
    val department: MyDepartmentResponse?,
    val favoriteCount: Int,
    val benefitUsageCount: Int,
)

data class MyCollegeResponse(
    val collegeId: Long,
    val name: String,
)

data class MyDepartmentResponse(
    val departmentId: Long,
    val name: String,
)

data class BenefitUsageResponse(
    val usageId: Long,
    val storeId: Long,
    val storeName: String,
    val benefitTitle: String,
    val usedAt: LocalDateTime,
)
