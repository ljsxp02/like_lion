package com.likelion.common.auth

interface CurrentUserProvider {
    fun currentUser(): CurrentUser
    fun currentUserOrNull(): CurrentUser? = runCatching(::currentUser).getOrNull()
    fun currentUserId(): Long = currentUser().userId
    fun currentUserIdOrNull(): Long? = currentUserOrNull()?.userId
}
