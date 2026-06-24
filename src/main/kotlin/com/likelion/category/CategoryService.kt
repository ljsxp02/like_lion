package com.likelion.category

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.domain.category.CollegeRepository
import com.likelion.domain.category.DepartmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CategoryService(
    private val collegeRepository: CollegeRepository,
    private val departmentRepository: DepartmentRepository,
) {
    @Transactional(readOnly = true)
    fun getColleges(): CollegeListResponse =
        CollegeListResponse(
            colleges = collegeRepository.findAllByOrderByIdAsc().map {
                CollegeResponse(collegeId = requireNotNull(it.id), name = it.name)
            },
        )

    @Transactional(readOnly = true)
    fun getDepartments(collegeId: Long?): DepartmentListResponse {
        if (collegeId != null && !collegeRepository.existsById(collegeId)) {
            throw ApiException(ErrorCode.COMMON_400)
        }

        val departments = if (collegeId == null) {
            departmentRepository.findAllByOrderByIdAsc()
        } else {
            departmentRepository.findAllByCollegeIdOrderByIdAsc(collegeId)
        }

        return DepartmentListResponse(
            departments = departments.map {
                DepartmentResponse(
                    departmentId = requireNotNull(it.id),
                    collegeId = it.collegeId,
                    name = it.name,
                )
            },
        )
    }
}
