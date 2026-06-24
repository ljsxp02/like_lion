package com.likelion.common.auth

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextCurrentUserProvider : CurrentUserProvider {
    override fun currentUser(): CurrentUser =
        SecurityContextHolder.getContext().authentication
            ?.takeIf { it.isAuthenticated }
            ?.principal as? CurrentUser
            ?: throw ApiException(ErrorCode.AUTH_001)
}
