package com.likelion.domain.benefitusage

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface BenefitUsageRepository : JpaRepository<BenefitUsageEntity, Long> {
    fun findAllByUserIdOrderByUsedAtDesc(userId: Long): List<BenefitUsageEntity>
    fun findAllByUserIdOrderByUsedAtDesc(userId: Long, pageable: Pageable): Page<BenefitUsageEntity>
    fun existsByUserIdAndQrTokenId(userId: Long, qrTokenId: Long): Boolean
    fun countByUserId(userId: Long): Long
}
