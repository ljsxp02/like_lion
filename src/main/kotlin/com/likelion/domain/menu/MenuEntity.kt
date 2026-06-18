package com.likelion.domain.menu

import com.likelion.domain.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "menus")
class MenuEntity(
    @Column(nullable = false)
    var storeId: Long,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Column(nullable = false)
    var isRepresentative: Boolean,

    @Column(nullable = false)
    var displayOrder: Int,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
