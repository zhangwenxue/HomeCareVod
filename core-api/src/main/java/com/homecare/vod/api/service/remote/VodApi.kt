package com.homecare.vod.api.service.remote

import com.google.gson.Gson
import com.homecare.vod.api.cfg.Configuration
import com.homecare.vod.api.entity.ApiResp
import com.homecare.vod.api.entity.OutGoingMsg
import com.homecare.vod.api.entity.OutgoingMsgType
import com.homecare.vod.api.entity.TRTCUserSig
import com.homecare.vod.api.entity.VodCallReqParam
import com.homecare.vod.api.entity.VodCallResp
import com.homecare.vod.api.http.retrofitService
import com.homecare.vod.api.http.withHttpResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VodApi {
    private val api by lazy {
        retrofitService<ApiService>({ Configuration.getBaseUrl() })
    }

    suspend fun getTRTCUserSig(userId: String): Result<TRTCUserSig> {
        return withHttpResult { api.getTRTCUserSig(userId) }
    }

    suspend fun requestVodCall(vodCallReqParam: VodCallReqParam): Result<ApiResp<VodCallResp>> {
        return runCatching { api.requestVodCall(vodCallReqParam) }
    }

    suspend fun sendOutgoingMsg(
        outgoingMsgType: OutgoingMsgType,
        registeredImId: String,
        patientName: String,
        vodCallResp: VodCallResp,
        imMsgPrefix: String = "",
    ): Result<Any> {
        val gson = Gson()
        val roomId = runCatching { vodCallResp.roomId.toInt() }.getOrElse { -1 }
        val msg = when (outgoingMsgType) {
            OutgoingMsgType.Dialing -> gson.toJson(
                OutGoingMsg(
                    "${imMsgPrefix}发起患者视频通话",
                    fromName = patientName,
                    roomId = roomId
                )
            )

            OutgoingMsgType.Answering -> gson.toJson(
                OutGoingMsg("已接通", roomId = roomId, type = "接通")
            )

            OutgoingMsgType.Hangup -> gson.toJson(
                OutGoingMsg(
                    "${imMsgPrefix}已挂断",
                    fromName = patientName,
                    roomId = roomId
                )
            )
        }

        val params = mapOf<String, String>(
            "messageContent" to msg,
            "messageType" to "${outgoingMsgType.msgType}",
            "receiveUserId" to vodCallResp.doctorId,
            "receiveUserRole" to "doctor",
            "sendTime" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date()),
            "sendUserId" to registeredImId,
            "sendUserRole" to "patient",
            "conversationId" to vodCallResp.conversationId
        )
        return withHttpResult { api.sendChatMsg(params) }
    }
}