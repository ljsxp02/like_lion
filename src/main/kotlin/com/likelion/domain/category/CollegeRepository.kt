package com.likelion.domain.category

import org.springframework.data.jpa.repository.JpaRepository

interface CollegeRepository : JpaRepository<CollegeEntity, Long> {
    fun findAllByOrderByIdAsc(): List<CollegeEntity>
}
