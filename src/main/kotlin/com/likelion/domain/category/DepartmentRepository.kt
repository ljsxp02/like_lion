package com.likelion.domain.category

import org.springframework.data.jpa.repository.JpaRepository

interface DepartmentRepository : JpaRepository<DepartmentEntity, Long> {
    fun findAllByOrderByIdAsc(): List<DepartmentEntity>
    fun findAllByCollegeIdOrderByIdAsc(collegeId: Long): List<DepartmentEntity>
}
