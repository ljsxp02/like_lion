package com.likelion.domain.store

import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository : JpaRepository<StoreEntity, Long> {
    fun findByIdAndIsActiveTrue(id: Long): StoreEntity?
    fun findByIdAndIsActiveTrueAndDeletedAtIsNull(id: Long): StoreEntity?
}
