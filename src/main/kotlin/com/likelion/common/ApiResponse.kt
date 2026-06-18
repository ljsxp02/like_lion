package com.likelion.common

data class ApiResponse<T>(
    val success: Boolean,
    val status: Int,
    val code: String,
    val message: String?,
    val data: T?,
) {
    companion object {
        fun <T> ok(code: String = "OK", message: String? = null, data: T): ApiResponse<T> =
            ApiResponse(
                success = true,
                status = 200,
                code = code,
                message = message,
                data = data,
            )

        fun <T> created(code: String = "CREATED", message: String? = null, data: T): ApiResponse<T> =
            ApiResponse(
                success = true,
                status = 201,
                code = code,
                message = message,
                data = data,
            )

        fun error(status: Int, code: String, message: String): ApiResponse<Nothing> =
            ApiResponse(
                success = false,
                status = status,
                code = code,
                message = message,
                data = null,
            )
    }
}
