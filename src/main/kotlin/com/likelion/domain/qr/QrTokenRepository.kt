package com.likelion.domain.qr

import org.springframework.data.jpa.repository.JpaRepository

interface QrTokenRepository : JpaRepository<QrTokenEntity, Long> {
    fun findByTokenAndIsActiveTrue(token: String): QrTokenEntity?
}
