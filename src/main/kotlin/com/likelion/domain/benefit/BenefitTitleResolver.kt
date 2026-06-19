package com.likelion.domain.benefit

import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 매장의 "현재(today) 기준 사용 가능한 활성 혜택 제목"을 조회하는 공통 컴포넌트.
 *
 * benefit_usages에는 benefit_id가 없어 사용 당시 혜택을 특정할 수 없으므로,
 * Favorite 목록 description과 혜택 사용 내역 benefitTitle 모두 매장의 현재 활성 혜택 제목을 사용한다.
 * 활성 혜택이 없으면 [FALLBACK]을 반환한다.
 */
@Component
class BenefitTitleResolver(
    private val benefitRepository: BenefitRepository,
) {
    /**
     * 여러 매장의 활성 혜택 제목을 한 번의 쿼리로 조회한다(목록 조회 N+1 방지).
     * 매장별로 id가 가장 작은 활성 혜택의 제목을 사용한다.
     */
    fun titlesByStoreId(storeIds: Collection<Long>, today: LocalDate = LocalDate.now()): Map<Long, String> {
        val ids = storeIds.toSet()
        if (ids.isEmpty()) {
            return emptyMap()
        }
        return benefitRepository
            .findActiveBenefits(storeIds = ids, today = today)
            .groupBy { it.storeId }
            .mapValues { (_, benefits) -> benefits.first().title }
    }

    /** 조회된 제목 맵에서 매장 제목을 꺼내되 없으면 [FALLBACK]을 반환한다. */
    fun titleOrFallback(titlesByStoreId: Map<Long, String>, storeId: Long): String =
        titlesByStoreId[storeId] ?: FALLBACK

    companion object {
        const val FALLBACK = "혜택 정보 없음"
    }
}
