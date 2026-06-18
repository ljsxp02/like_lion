package com.likelion.domain.category

import org.springframework.data.jpa.repository.JpaRepository

interface DepartmentRepository : JpaRepository<DepartmentEntity, Long> {
    fun findAllByCollegeId(collegeId: Long): List<DepartmentEntity>
}
