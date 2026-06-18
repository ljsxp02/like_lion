package com.likelion.category

data class CollegeResponse(
    val collegeId: Long,
    val name: String,
)

data class CollegeListResponse(
    val colleges: List<CollegeResponse>,
)

data class DepartmentResponse(
    val departmentId: Long,
    val collegeId: Long,
    val name: String,
)

data class DepartmentListResponse(
    val departments: List<DepartmentResponse>,
)
