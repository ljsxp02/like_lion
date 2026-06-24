package com.likelion.domain.store

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StoreRepository : JpaRepository<StoreEntity, Long> {
    fun findByIdAndIsActiveTrue(id: Long): StoreEntity?
    fun findByIdAndIsActiveTrueAndDeletedAtIsNull(id: Long): StoreEntity?

    @Query(
        value = """
            select distinct s
            from StoreEntity s
            where s.isActive = true
              and s.deletedAt is null
              and (:keyword is null or lower(s.name) like lower(concat('%', :keyword, '%')))
              and (
                :collegeId is null
                or exists (
                    select b.id
                    from BenefitEntity b
                    join b.targetColleges c
                    where b.storeId = s.id
                      and b.isActive = true
                      and b.startDate <= current_date
                      and b.endDate >= current_date
                      and c.id = :collegeId
                )
              )
              and (
                :departmentId is null
                or exists (
                    select b.id
                    from BenefitEntity b
                    join b.targetDepartments d
                    where b.storeId = s.id
                      and b.isActive = true
                      and b.startDate <= current_date
                      and b.endDate >= current_date
                      and d.id = :departmentId
                )
              )
              and (
                :favoriteOnly = false
                or exists (
                    select f.id
                    from FavoriteEntity f
                    where f.storeId = s.id
                      and f.userId = :userId
                )
              )
            order by s.id asc
        """,
        countQuery = """
            select count(distinct s)
            from StoreEntity s
            where s.isActive = true
              and s.deletedAt is null
              and (:keyword is null or lower(s.name) like lower(concat('%', :keyword, '%')))
              and (
                :collegeId is null
                or exists (
                    select b.id
                    from BenefitEntity b
                    join b.targetColleges c
                    where b.storeId = s.id
                      and b.isActive = true
                      and b.startDate <= current_date
                      and b.endDate >= current_date
                      and c.id = :collegeId
                )
              )
              and (
                :departmentId is null
                or exists (
                    select b.id
                    from BenefitEntity b
                    join b.targetDepartments d
                    where b.storeId = s.id
                      and b.isActive = true
                      and b.startDate <= current_date
                      and b.endDate >= current_date
                      and d.id = :departmentId
                )
              )
              and (
                :favoriteOnly = false
                or exists (
                    select f.id
                    from FavoriteEntity f
                    where f.storeId = s.id
                      and f.userId = :userId
                )
              )
        """,
    )
    fun searchVisibleStores(
        @Param("collegeId") collegeId: Long?,
        @Param("departmentId") departmentId: Long?,
        @Param("keyword") keyword: String?,
        @Param("favoriteOnly") favoriteOnly: Boolean,
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): Page<StoreEntity>

    @Query(
        """
            select distinct s
            from StoreEntity s
            where s.isActive = true
              and s.deletedAt is null
              and (
                :collegeId is null
                or exists (
                    select b.id
                    from BenefitEntity b
                    join b.targetColleges c
                    where b.storeId = s.id
                      and b.isActive = true
                      and b.startDate <= current_date
                      and b.endDate >= current_date
                      and c.id = :collegeId
                )
              )
              and (
                :departmentId is null
                or exists (
                    select b.id
                    from BenefitEntity b
                    join b.targetDepartments d
                    where b.storeId = s.id
                      and b.isActive = true
                      and b.startDate <= current_date
                      and b.endDate >= current_date
                      and d.id = :departmentId
                )
              )
            order by s.id asc
        """,
    )
    fun findVisibleStoresForMap(
        @Param("collegeId") collegeId: Long?,
        @Param("departmentId") departmentId: Long?,
    ): List<StoreEntity>

    fun findByNameContainingIgnoreCaseAndIsActiveTrueAndDeletedAtIsNullOrderByIdAsc(
        name: String,
        pageable: Pageable,
    ): List<StoreEntity>
}
