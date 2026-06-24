package com.likelion.common.config

import com.likelion.auth.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authorization = request.getHeader("Authorization")
        if (authorization?.startsWith(BEARER_PREFIX) == true) {
            runCatching {
                jwtService.parseAccessToken(authorization.removePrefix(BEARER_PREFIX).trim())
            }.onSuccess { currentUser ->
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(currentUser, null, emptyList())
            }.onFailure {
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}
