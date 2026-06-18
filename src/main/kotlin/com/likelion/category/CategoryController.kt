package com.likelion.category

import com.likelion.common.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class CategoryController(
    private val categoryService: CategoryService,
) {
    @GetMapping("/colleges")
    fun getColleges(): ApiResponse<CollegeListResponse> =
        ApiResponse.ok(message = "단과대 목록을 조회했습니다.", data = categoryService.getColleges())

    @GetMapping("/departments")
    fun getDepartments(
        @RequestParam(required = false) collegeId: Long?,
    ): ApiResponse<DepartmentListResponse> =
        ApiResponse.ok(message = "학과 목록을 조회했습니다.", data = categoryService.getDepartments(collegeId))
}
