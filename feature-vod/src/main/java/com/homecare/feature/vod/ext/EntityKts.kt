package com.homecare.feature.vod.ext

import androidx.annotation.Keep
import com.homecare.feature.vod.HomeCareVodSDK.ORIENTATION_LANDSCAPE
import com.homecare.vod.api.entity.VodCallResp


private const val IM_EVENT_VOD_CONNECTED = "已接通"
private const val IM_EVENT_VOD_REJECTED = "已拒接"
private const val IM_EVENT_VOD_HANGED_UP = "已挂断"
private const val IM_EVENT_VOD_SESSION_ENDED = "会话已结束"
private const val IM_EVENT_VOD_INCOMING_CALL = "医生发起视频通话"

@Keep
data class VodSDKConfig(
    val screenOrientation: Int = ORIENTATION_LANDSCAPE,
    val release: Boolean = true,
    val kickOfflineCallback: () -> Unit,
)

sealed class LoginState(val userId: String) {
    @Keep
    data object Idle : LoginState("")

    @Keep
    data class Connecting(val id: String) : LoginState(id)

    @Keep
    data class ConnectSuccess(val id: String) : LoginState(id)

    @Keep
    data class ConnectFailed(val id: String, val code: Int, val error: String?) : LoginState(id)

    @Keep
    data class UserSigExpired(val id: String) : LoginState(id)

    @Keep
    data class KickedOffline(val id: String) : LoginState(id)

    @Keep
    data class LoggedOut(val id: String) : LoginState(id)
}

@Keep
data class VodCallReq(
    val registeredIdCardNo: String = "",
    val registeredUserName: String = "",
    val registeredImId: String = "",
    val patientIdCardNo: String? = null,
    val patientName: String = "",
    val patientImId: String,
    val token: String? = null,
    val jwtToken: String? = null,
    val fileList: List<String> = emptyList(),
    val symptomsDescription: String = "",
    val hasEcg: Boolean = false
)


sealed interface CallingState {
    @Keep
    data object Idle : CallingState

    @Keep
    data class Dialing(val response: VodCallResp?) : CallingState

    @Keep
    data class Busy(val lineNumber: Int) : CallingState

    @Keep
    data object Connected : CallingState

    @Keep
    data object HangedUp : CallingState

    @Keep
    data object Rejected : CallingState

    @Keep
    data object SessionEnd : CallingState

    @Keep
    data class Answering(val response: VodCallResp?) : CallingState

    @Keep
    data class OnError(val error: Throwable) : CallingState
}

fun CallingState.isBusy(): Boolean {
    return when (this) {
        is CallingState.Idle, is CallingState.HangedUp, is CallingState.Rejected, is CallingState.OnError, is CallingState.Busy -> false
        else -> true
    }
}


@Keep
data class ImMsg(
    val msg: String?,
    val type: String?,
    val id: String,
    val roomId: String,
    val conversationId: String,
    val fromName: String,
    val avatar: String,
    val doctorId: String,
    val patientId: String,
    val patientName: String,
    val jumping: Boolean = true
)

@Keep
data class ImMsgExt(
    val content: String,
    val msgId: String,
    val receiveUserId: String,
    val receiverHeadPic: String,
    val sendTime: String,
    val sendUserId: String,
    val sendUserName: String,
    val senderHeadPic: String,
    val type: String
)

@Keep
sealed class VodImEvent(val imMsg: ImMsg, val msgExt: ImMsgExt?, val msgType: String?) {
    @Keep
    data class VodConnectedEvent(val msg: ImMsg, val ext: ImMsgExt?) :
        VodImEvent(msg, ext, IM_EVENT_VOD_CONNECTED)

    @Keep
    data class VodRejectedEvent(val msg: ImMsg, val ext: ImMsgExt?) :
        VodImEvent(msg, ext, IM_EVENT_VOD_REJECTED)

    @Keep
    data class VodHangupEvent(val msg: ImMsg, val ext: ImMsgExt?) :
        VodImEvent(msg, ext, IM_EVENT_VOD_HANGED_UP)

    @Keep
    data class VodEndEvent(val msg: ImMsg, val ext: ImMsgExt?) :
        VodImEvent(msg, ext, IM_EVENT_VOD_SESSION_ENDED)

    @Keep
    data class VodIncomingCallEvent(val msg: ImMsg, val ext: ImMsgExt?) :
        VodImEvent(msg, ext, IM_EVENT_VOD_INCOMING_CALL)

    @Keep
    data class UnexpectedEvent(val msg: ImMsg, val ext: ImMsgExt?) : VodImEvent(msg, ext, msg.msg)
}

fun createVodEvent(msg: ImMsg, ext: ImMsgExt?): VodImEvent {
    return when (msg.msg) {
        IM_EVENT_VOD_CONNECTED -> VodImEvent.VodConnectedEvent(msg, ext)
        IM_EVENT_VOD_REJECTED -> VodImEvent.VodRejectedEvent(msg, ext)
        IM_EVENT_VOD_HANGED_UP -> VodImEvent.VodHangupEvent(msg, ext)
        IM_EVENT_VOD_SESSION_ENDED -> VodImEvent.VodEndEvent(msg, ext)
        IM_EVENT_VOD_INCOMING_CALL -> VodImEvent.VodIncomingCallEvent(msg, ext)
        else -> VodImEvent.UnexpectedEvent(msg, ext)
    }
}