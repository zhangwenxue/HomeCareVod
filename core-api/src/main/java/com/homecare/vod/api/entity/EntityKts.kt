package com.homecare.vod.api.entity

import androidx.annotation.Keep

internal const val HTTP_OK = 200

@Keep
data class ApiResp<T>(
    val code: Int,
    val msg: String?,
    val data: T?
) {
    override fun toString(): String {
        return "{code:$code,msg:$msg,data:$data}"
    }

    fun isSuccess() = code == HTTP_OK
}

fun <T> ApiResp<T>.toResult(): Result<T> {
    if (code != HTTP_OK) return Result.failure(Throwable("Error response code:$code ,raw:$this"))
    if (data == null) return Result.failure(NullPointerException("Null response value,raw:$this"))
    return Result.success(data)
}

@Keep
data class TRTCUserSig(
    val expireTime: Int,
    val identifier: String,
    val initTime: Int,
    val sdkAppId: Int,
    val userSig: String
)

@Keep
data class VodCallReqParam(
    val patientId: String,
    val refId: String? = null,
    val information: String = "",
    val imgList: List<String> = listOf(),
    val jwtToken: String? = null,
    val token: String? = null,
    val trtcWeight: Int = -1,
    val viewEcgData: Int = -1,
    val level: Int = 0,
    val extType: String = "",
    val extVal: String? = null
)

@Keep
data class VodCallResp(
    val conversationId: String,
    val doctorHeadImg: String?,
    val doctorId: String,
    val doctorName: String?,
    val deptName: String?,
    val titleName: String?,
    val patientId: String,
    val patientName: String,
    val refId: String,
    val refPatientName: String,
    val roomId: String,
    val jumping: Boolean = true
)

enum class OutgoingMsgType(val msgType: Int, val desc: String) {
    Dialing(34, "发起视频通话"),
    Answering(35, "接听视频通话"),
    Hangup(36, "挂断视频通话")
}

data class OutGoingMsg(
    val msg: String? = "",
    val fromName: String? = "",
    val roomId: Int? = 1,
    var type: String? = "",
    val jumping: Boolean = true
)

