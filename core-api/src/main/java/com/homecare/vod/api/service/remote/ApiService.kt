package com.homecare.vod.api.service.remote

import com.homecare.vod.api.entity.ApiResp
import com.homecare.vod.api.entity.TRTCUserSig
import com.homecare.vod.api.entity.VodCallReqParam
import com.homecare.vod.api.entity.VodCallResp
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {
    @POST("js/api/baiyang/tencent/im/usersign")
    suspend fun getTRTCUserSig(@Query("userId") userId: String): ApiResp<TRTCUserSig>

    @POST("js/api/wacdih/online/onlineConsultationApi/addRealTimeRevisit")
    suspend fun requestVodCall(@Body request: VodCallReqParam): ApiResp<VodCallResp>

    @POST("js/api/wacdih/single/chat/sendMsg")
    suspend fun sendChatMsg(@QueryMap map: Map<String, String>): ApiResp<Any>
}