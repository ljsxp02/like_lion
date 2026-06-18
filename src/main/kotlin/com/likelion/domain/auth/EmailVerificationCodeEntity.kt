package com.likelion.domain.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "email_verification_codes")
class EmailVerificationCodeEntity(
    @Column(nullable = false, length = 120)
    var email: String,

    @Column(nullable = false, length = 20)
    var code: String,

    @Column(length = 255)
    var verificationToken: String? = null,

    @Column(nullable = false)
    var expiresAt: LocalDateTime,

    @Column(nullable = false)
    var resendAvailableAt: LocalDateTime,

    @Column(nullable = false)
    var verified: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
