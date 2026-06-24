package com.likelion.domain.benefit

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface BenefitRepository : JpaRepository<BenefitEntity, Long> {
    fun findAllByStoreIdAndIsActiveTrue(storeId: Long): List<BenefitEntity>

    @Query(
        """
            select b
            from BenefitEntity b
            where b.storeId = :storeId
              and b.isActive = true
              and b.startDate <= :today
              and b.endDate >= :today
            order by b.id asc
        """,
    )
    fun findCurrentBenefitsByStoreId(
        @Param("storeId") storeId: Long,
        @Param("today") today: LocalDate,
    ): List<BenefitEntity>

    /**
     * 여러 매장의 today 기준 사용 가능한 활성 혜택을 한 번에 조회한다.
     * 매장/혜택 id 오름차순으로 정렬해, 호출 측에서 매장별 첫 혜택을 대표로 사용할 수 있게 한다.
     */
    @Query(
        """
            select b
            from BenefitEntity b
            where b.storeId in :storeIds
              and b.isActive = true
              and b.startDate <= :today
              and b.endDate >= :today
            order by b.storeId asc, b.id asc
        """,
    )
    fun findActiveBenefits(
        @Param("storeIds") storeIds: Collection<Long>,
        @Param("today") today: LocalDate,
    ): List<BenefitEntity>
}
