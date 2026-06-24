package com.likelion.domain.favorite

import com.likelion.domain.store.StoreEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FavoriteRepository : JpaRepository<FavoriteEntity, Long> {
    fun existsByUserIdAndStoreId(userId: Long, storeId: Long): Boolean
    fun findByUserIdAndStoreId(userId: Long, storeId: Long): FavoriteEntity?
    fun findAllByUserId(userId: Long): List<FavoriteEntity>

    @Query(
        """
            select f.storeId
            from FavoriteEntity f
            where f.userId = :userId
              and f.storeId in :storeIds
        """,
    )
    fun findFavoriteStoreIds(
        @Param("userId") userId: Long,
        @Param("storeIds") storeIds: Collection<Long>,
    ): Set<Long>

    @Query(
        value = """
            select s
            from FavoriteEntity f
            join StoreEntity s on s.id = f.storeId
            where f.userId = :userId
              and s.isActive = true
              and s.deletedAt is null
            order by f.createdAt desc, f.id desc
        """,
        countQuery = """
            select count(s)
            from FavoriteEntity f
            join StoreEntity s on s.id = f.storeId
            where f.userId = :userId
              and s.isActive = true
              and s.deletedAt is null
        """,
    )
    fun findVisibleFavoriteStoresByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): Page<StoreEntity>

    @Query(
        """
            select count(s)
            from FavoriteEntity f
            join StoreEntity s on s.id = f.storeId
            where f.userId = :userId
              and s.isActive = true
              and s.deletedAt is null
        """,
    )
    fun countVisibleFavoriteStoresByUserId(
        @Param("userId") userId: Long,
    ): Long
}
