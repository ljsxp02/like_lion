package com.likelion.domain.store

import com.likelion.domain.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "stores")
class StoreEntity(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 255)
    var address: String,

    @Column(length = 255)
    var location: String? = null,

    @Column(length = 50)
    var contact: String? = null,

    @Column(length = 500)
    var thumbnailUrl: String? = null,

    @Column
    var latitude: Double? = null,

    @Column
    var longitude: Double? = null,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column
    var deletedAt: LocalDateTime? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
