package com.likelion.auth

import com.likelion.common.auth.CurrentUser
import com.likelion.domain.user.UserEntity
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${app.jwt.secret}")
    secret: String,
    @Value("\${app.jwt.access-token-expiration-seconds}")
    accessTokenExpirationSeconds: Long,
) {
    init {
        require(secret.length >= MINIMUM_SECRET_LENGTH) {
            "JWT_SECRET must contain at least $MINIMUM_SECRET_LENGTH characters."
        }
    }

    val accessTokenExpirationSeconds: Long = accessTokenExpirationSeconds

    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

    fun createAccessToken(user: UserEntity): String {
        val now = Instant.now()
        return Jwts.builder()
            .subject(requireNotNull(user.id).toString())
            .claim("email", user.email)
            .claim("userType", user.userType.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessTokenExpirationSeconds)))
            .signWith(signingKey)
            .compact()
    }

    fun parseAccessToken(token: String): CurrentUser {
        val claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return CurrentUser(
            userId = claims.subject.toLong(),
            email = claims["email", String::class.java],
            userType = claims["userType", String::class.java],
        )
    }

    private companion object {
        const val MINIMUM_SECRET_LENGTH = 32
    }
}
