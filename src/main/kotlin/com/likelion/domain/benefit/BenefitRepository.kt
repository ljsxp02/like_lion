package com.likelion.domain.benefit

import org.springframework.data.jpa.repository.JpaRepository

interface BenefitRepository : JpaRepository<BenefitEntity, Long> {
    fun findAllByStoreIdAndIsActiveTrue(storeId: Long): List<BenefitEntity>
}
