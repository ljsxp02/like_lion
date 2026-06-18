package com.likelion.category

import org.springframework.stereotype.Service

@Service
class CategoryService {
    fun getColleges(): CollegeListResponse =
        CollegeListResponse(
            colleges = listOf(
                CollegeResponse(collegeId = 1, name = "소프트웨어융합대학"),
            ),
        )

    fun getDepartments(collegeId: Long?): DepartmentListResponse =
        DepartmentListResponse(
            departments = listOf(
                DepartmentResponse(departmentId = 3, collegeId = 1, name = "컴퓨터정보공학부"),
            ).filter { collegeId == null || it.collegeId == collegeId },
        )
}
