package com.likelion.common.auth

data class CurrentUser(
    val userId: Long,
    val email: String,
    val userType: String,
)
