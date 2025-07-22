package com.homecare.feature.vod.exception

internal const val INVALID_USER_ID = 0x01
internal const val ERROR_USER_SIG = 0x02

sealed class VodException(val code: Int, override val message: String) :
    Exception(message) {
    data class InvalidUserIdException(val id: String?) : VodException(
        INVALID_USER_ID, "Invalid user id:$id"
    )

    data class UserSigException(override val message: String) : VodException(
        ERROR_USER_SIG, message
    )

    data class TRTCException(val errorCode: Int, override val message: String) :
        VodException(errorCode, message)
}