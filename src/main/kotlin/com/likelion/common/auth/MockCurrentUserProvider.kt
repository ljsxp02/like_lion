package com.likelion.common.auth

import org.springframework.stereotype.Component

@Component
class MockCurrentUserProvider : CurrentUserProvider {
    override fun currentUser(): CurrentUser =
        CurrentUser(
            userId = 1024,
            email = "student@kw.ac.kr",
            userType = "STUDENT",
        )
}
