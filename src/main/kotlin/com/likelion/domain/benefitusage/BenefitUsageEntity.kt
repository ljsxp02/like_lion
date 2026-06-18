package com.likelion.domain.benefitusage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "benefit_usages")
class BenefitUsageEntity(
    @Column(nullable = false)
    var userId: Long,

    @Column(nullable = false)
    var storeId: Long,

    @Column(nullable = false)
    var qrTokenId: Long,

    @Column(nullable = false)
    var usedAt: LocalDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
