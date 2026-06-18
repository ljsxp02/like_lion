package com.likelion.domain.qr

import com.likelion.domain.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "qr_tokens")
class QrTokenEntity(
    @Column(nullable = false)
    var storeId: Long,

    @Column(nullable = false, unique = true, length = 255)
    var token: String,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column
    var issuedAt: LocalDateTime? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
