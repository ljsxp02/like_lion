package com.likelion.domain.auth

import org.springframework.data.jpa.repository.JpaRepository

interface EmailVerificationCodeRepository : JpaRepository<EmailVerificationCodeEntity, Long> {
    fun findFirstByEmailOrderByIdDesc(email: String): EmailVerificationCodeEntity?
    fun findByVerificationToken(verificationToken: String): EmailVerificationCodeEntity?
}
