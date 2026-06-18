package com.likelion.domain.menu

import org.springframework.data.jpa.repository.JpaRepository

interface MenuRepository : JpaRepository<MenuEntity, Long> {
    fun findAllByStoreIdOrderByDisplayOrderAsc(storeId: Long): List<MenuEntity>
}
