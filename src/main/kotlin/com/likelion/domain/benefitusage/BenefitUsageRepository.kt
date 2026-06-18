package com.likelion.domain.benefitusage

import org.springframework.data.jpa.repository.JpaRepository

interface BenefitUsageRepository : JpaRepository<BenefitUsageEntity, Long> {
    fun findAllByUserIdOrderByUsedAtDesc(userId: Long): List<BenefitUsageEntity>
    fun existsByUserIdAndQrTokenId(userId: Long, qrTokenId: Long): Boolean
}
