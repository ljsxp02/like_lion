package com.likelion.domain.favorite

import org.springframework.data.jpa.repository.JpaRepository

interface FavoriteRepository : JpaRepository<FavoriteEntity, Long> {
    fun existsByUserIdAndStoreId(userId: Long, storeId: Long): Boolean
    fun findByUserIdAndStoreId(userId: Long, storeId: Long): FavoriteEntity?
    fun findAllByUserId(userId: Long): List<FavoriteEntity>
}
