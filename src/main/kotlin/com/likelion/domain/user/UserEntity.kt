package com.likelion.domain.user

import com.likelion.domain.common.BaseTimeEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, unique = true, length = 120)
    var email: String,

    @Column(nullable = false, length = 255)
    var passwordHash: String,

    @Column(nullable = false, length = 50)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var userType: UserType,

    @Column(nullable = false)
    var isEmailVerified: Boolean = false,

    @Column
    var collegeId: Long? = null,

    @Column
    var departmentId: Long? = null,

    @Column
    var storeId: Long? = null,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}

enum class UserType {
    STUDENT,
    OWNER,
    ADMIN,
}
