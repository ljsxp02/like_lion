package com.likelion.common.auth

interface CurrentUserProvider {
    fun currentUser(): CurrentUser
    fun currentUserId(): Long = currentUser().userId
}
