package com.homecare.feature.vod

import android.content.Context
import android.content.Intent
import android.media.SoundPool
import android.util.Log
import com.google.gson.Gson
import com.homecare.feature.vod.exception.ERROR_USER_SIG
import com.homecare.feature.vod.exception.INVALID_USER_ID
import com.homecare.feature.vod.ext.CallingState
import com.homecare.feature.vod.ext.ImMsg
import com.homecare.feature.vod.ext.ImMsgExt
import com.homecare.feature.vod.ext.LoginState
import com.homecare.feature.vod.ext.VodCallReq
import com.homecare.feature.vod.ext.VodImEvent
import com.homecare.feature.vod.ext.VodSDKConfig
import com.homecare.feature.vod.ext.createVodEvent
import com.homecare.feature.vod.ext.errorMessage
import com.homecare.feature.vod.ext.isBusy
import com.homecare.feature.vod.ext.runOnUi
import com.homecare.feature.vod.ui.VodCallActivity
import com.homecare.vod.api.cfg.Configuration
import com.homecare.vod.api.entity.OutgoingMsgType
import com.homecare.vod.api.entity.VodCallReqParam
import com.homecare.vod.api.entity.VodCallResp
import com.homecare.vod.api.service.remote.VodApi
import com.tencent.cloud.tuikit.engine.call.TUICallDefine
import com.tencent.cloud.tuikit.engine.common.TUICommonDefine
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMMessage
import com.tencent.qcloud.tuicore.TUILogin
import com.tencent.qcloud.tuicore.interfaces.TUICallback
import com.tencent.qcloud.tuicore.interfaces.TUILoginListener
import com.tencent.qcloud.tuikit.tuicallkit.TUICallKit
import com.tencent.qcloud.tuikit.tuicallkit.manager.CallManager
import com.tencent.qcloud.tuikit.tuicallkit.manager.hybird.CallBridge
import com.trtc.tuikit.common.livedata.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object HomeCareVodSDK {
    const val ORIENTATION_PORTRAIT = 0
    const val ORIENTATION_LANDSCAPE = 1
    const val ORIENTATION_AUTO = 2
    private const val SDK_APP_ID = 1600027488
    private const val AVATAR =
        "https://liteav.sdk.qcloud.com/app/res/picture/voiceroom/avatar/user_avatar1.png"
    private val scope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    private val soundPool by lazy {
        SoundPool.Builder().setMaxStreams(1).build()
    }

    private var soundId = -1
    private var streamID = -1

    private var config: VodSDKConfig = VodSDKConfig {}
    private var outComingCall = true
    private lateinit var appContext: Context
    private var vodReq: VodCallReq? = null
    private var vodResp: VodCallResp? = null

    private val _loginStateFlow = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginStateFlow: StateFlow<LoginState> = _loginStateFlow.asStateFlow()

    private val _callingStateFlow = MutableStateFlow<CallingState>(CallingState.Idle)
    val callingStateFlow = _callingStateFlow.asStateFlow()

    private val loginListener = object : TUILoginListener() {
        override fun onKickedOffline() {
            super.onKickedOffline()
            stopRing()
            trace("Trtc:${TUILogin.getLoginUser()} on kicked offline")
            removeIMListener()
            config.kickOfflineCallback.invoke()
            _loginStateFlow.update { LoginState.KickedOffline(TUILogin.getLoginUser()) }
            _loginStateFlow.update { LoginState.Idle }
        }

        override fun onConnecting() {
            trace("Trtc:${TUILogin.getLoginUser()} on connecting")
            _loginStateFlow.update { LoginState.Connecting(TUILogin.getLoginUser()) }
        }

        override fun onConnectSuccess() {
            trace("Trtc:${TUILogin.getLoginUser()} on connect success")
            addIMListener()
            _loginStateFlow.update { LoginState.ConnectSuccess(TUILogin.getLoginUser()) }
        }

        override fun onConnectFailed(code: Int, error: String?) {
            stopRing()
            error("Trtc:${TUILogin.getLoginUser()} on connect failed,code:$code,error:$error")
            _loginStateFlow.update {
                LoginState.ConnectFailed(
                    TUILogin.getLoginUser(),
                    code,
                    error
                )
            }
            _loginStateFlow.update { LoginState.Idle }
            removeIMListener()
        }

        override fun onUserSigExpired() {
            val userId = TUILogin.getLoginUser()
            trace("Trtc:$userId on user sig expired,login automatically")
            _loginStateFlow.update { LoginState.UserSigExpired(TUILogin.getLoginUser()) }
            login(userId)
        }
    }

    private val v2TIMAdvancedMsgListener = object : V2TIMAdvancedMsgListener() {
        override fun onRecvNewMessage(msg: V2TIMMessage?) {
            super.onRecvNewMessage(msg)
            trace("Trtc: received new msg:\n$msg")
            if (msg?.msgID == null) return
            when (msg.elemType) {
                V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM -> {
                    val imMsg = runCatching {
                        Gson().fromJson(
                            String(msg.customElem.data),
                            ImMsg::class.java
                        )
                    }.getOrNull() ?: return

                    val imMsgExt =
                        runCatching {
                            Gson().fromJson(
                                String(msg.customElem.extension),
                                ImMsgExt::class.java
                            )
                        }.getOrNull()
                    val vodEvent = createVodEvent(imMsg, imMsgExt)
                    if (vodEvent is VodImEvent.UnexpectedEvent) {
                        error("Trtc: unexpected im event $vodEvent")
                        return
                    }
                    when (vodEvent) {
                        is VodImEvent.VodConnectedEvent -> {
                            stopRing()
                            if (outComingCall) {
                                // 主动加入视频聊天
                                joinInGroupCall(vodResp)
                            }
                        }

                        is VodImEvent.VodEndEvent -> {
                            stopRing()
                            _callingStateFlow.update { CallingState.SessionEnd }
                            _callingStateFlow.update { CallingState.Idle }
                        }

                        is VodImEvent.VodHangupEvent -> {
                            stopRing()
                            _callingStateFlow.update { CallingState.HangedUp }
                            _callingStateFlow.update { CallingState.Idle }
                        }

                        is VodImEvent.VodIncomingCallEvent -> {
                            playRing()
                            outComingCall = false
                            vodReq = VodCallReq(
                                registeredIdCardNo = "",
                                registeredUserName = "",
                                registeredImId = TUILogin.getLoginUser(),
                                patientIdCardNo = "",
                                patientName = vodEvent.msg.patientName,
                                patientImId = "",
                                token = ""
                            )

                            vodResp = VodCallResp(
                                conversationId = vodEvent.msg.conversationId,
                                doctorHeadImg = vodEvent.msg.avatar,
                                doctorId = vodEvent.msg.doctorId,
                                doctorName = vodEvent.msg.fromName,
                                deptName = "",
                                titleName = "",
                                patientId = TUILogin.getLoginUser(),
                                patientName = vodEvent.msg.patientName,
                                refId = "",
                                refPatientName = "",
                                roomId = vodEvent.msg.roomId,
                                jumping = true
                            )
                            _callingStateFlow.update { CallingState.Answering(vodResp) }
                            startTargetActivity(appContext)
                        }

                        is VodImEvent.VodRejectedEvent -> {
                            if (outComingCall) {
                                // 医生拒接
                                _callingStateFlow.update { CallingState.Rejected }
                                _callingStateFlow.update { CallingState.Idle }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    init {
        TUILogin.addLoginListener(loginListener)
    }

    @JvmStatic
    fun setup(
        context: Context,
        config: VodSDKConfig,
    ) {
        scope.runOnUi {
            this.config = config
            CallBridge.instance.setScreenOrientation(config.screenOrientation)
            observerCallState()
            appContext = context.applicationContext
            Configuration.useProdServer = config.release
            if (soundId == -1) {
                soundId = soundPool.load(appContext, R.raw.ring, 1)
            }
        }
    }

    fun login(userId: String, callback: (Boolean) -> Unit = {}) {
        scope.runOnUi {
            trace("Trtc:login($userId) request")
            if (TUILogin.isUserLogined() && TUILogin.getUserId() == userId) {
                trace("Trtc:$userId already logged")
                scope.runOnUi { callback(true) }
                return@runOnUi
            }
            if (userId.isBlank()) {
                error("Trtc:login error,userId is black:$userId")
                scope.runOnUi { callback(false) }
                _loginStateFlow.update {
                    LoginState.ConnectFailed(
                        TUILogin.getLoginUser(),
                        INVALID_USER_ID,
                        "Invalid user id"
                    )
                }
                return@runOnUi
            }

            scope.launch {
                _loginStateFlow.update { LoginState.Connecting(userId) }
                trace("Srv:request user sig($userId)")
                val userSig = VodApi.getTRTCUserSig(userId)
                if (userSig.isFailure) {
                    error("Srv:request sig failure", userSig.exceptionOrNull())
                    LoginState.ConnectFailed(
                        TUILogin.getLoginUser(),
                        ERROR_USER_SIG,
                        "Error userSig:${userSig.exceptionOrNull()?.message}"
                    )
                    scope.runOnUi { callback(false) }
                    return@launch
                }
                trace("Srv:request user sig success")
                trace("Trtc:login to trtc")
                TUILogin.login(
                    appContext,
                    SDK_APP_ID,
                    userId,
                    userSig.getOrNull()?.userSig ?: "",
                    object : TUICallback() {
                        override fun onSuccess() {
                            trace("Trtc:${userId} login success")
                            scope.runOnUi { callback(true) }
                            _loginStateFlow.update { LoginState.ConnectSuccess(TUILogin.getLoginUser()) }
                        }

                        override fun onError(errorCode: Int, errorMessage: String?) {
                            error("Trtc:${userId} login error,code:$errorCode,msg:$errorMessage")
                            scope.runOnUi { callback(false) }
                            _loginStateFlow.update {
                                LoginState.ConnectFailed(
                                    TUILogin.getLoginUser(),
                                    errorCode,
                                    errorMessage
                                )
                            }
                            _loginStateFlow.update { LoginState.Idle }
                        }
                    })
            }
        }
    }

    @JvmStatic
    fun logout(action: (Boolean) -> Unit = {}) {
        scope.runOnUi {
            trace("Trtc:logout")
            TUILogin.logout(object : TUICallback() {
                override fun onSuccess() {
                    trace("Trtc:logout success")
                    _loginStateFlow.update { LoginState.LoggedOut(TUILogin.getLoginUser()) }
                    action(true)
                }

                override fun onError(errorCode: Int, errorMessage: String?) {
                    error("Trtc:logout error,code:$errorCode,message:$errorMessage")
                    action(false)
                }
            })
        }
    }

    @JvmStatic
    fun callWithJwt(
        jwtToken: String,
        registeredUserId: String,
        patientId: String,
        symptomsDescription: String = "",
        fileList: List<String> = emptyList(),
        hasEcg: Boolean = false
    ) {
        val request = VodCallReq(
            registeredImId = registeredUserId,
            patientImId = patientId,
            symptomsDescription = symptomsDescription,
            jwtToken = jwtToken,
            fileList = fileList,
            hasEcg = hasEcg
        )
        scope.runOnUi {
            dialInternal(request)
        }
    }

    @JvmStatic
    fun call(
        token: String,
        registeredUserId: String,
        patientId: String,
        symptomsDescription: String = "",
        fileList: List<String> = emptyList(),
        hasEcg: Boolean = false,
        agency: String? = null
    ) {
        val request = VodCallReq(
            token = token,
            registeredImId = registeredUserId,
            patientImId = patientId,
            symptomsDescription = symptomsDescription,
            fileList = fileList,
            hasEcg = hasEcg
        )
        scope.runOnUi {
            dialInternal(request, false, agency)
        }
    }

    private fun dialInternal(
        request: VodCallReq,
        emergency: Boolean = false,
        agency: String? = null
    ) {
        if (_callingStateFlow.value.isBusy()) return
        outComingCall = true
        this.vodReq = request
        trace("Trtc:set call kit info")
        setCallkitInfo(request)
        login(request.registeredImId) {
            if (!it) {
                val err = "登录失败"
                _callingStateFlow.update { CallingState.OnError(Throwable(err)) }
                _callingStateFlow.update { CallingState.Idle }
                return@login
            }
            startTargetActivity(appContext)
            trace("Srv: start vod request")
            playRing()
            scope.launch {
                _callingStateFlow.update { CallingState.Dialing(null) }
                val extVal = agency?.let { name -> "{\"orgName\":\"$name\"}" }
                val req = VodCallReqParam(
                    patientId = request.patientImId,
                    //refId = request.patientImId,
                    imgList = request.fileList,
                    jwtToken = request.jwtToken,
                    token = request.token,
                    viewEcgData = if (request.hasEcg) 1 else -1,
                    information = request.symptomsDescription,
                    level = if (emergency) 99 else 0,
                    extVal = extVal
                )
                val respResult = VodApi.requestVodCall(req)
                val resp = respResult.getOrNull()
                if (respResult.isFailure) {
                    val error = Throwable("addRealTimeRevisit exception:${respResult.errorMessage}")
                    error(error.message ?: "")
                    stopRing()
                    _callingStateFlow.update { CallingState.OnError(error) }
                    _callingStateFlow.update { CallingState.Idle }
                    return@launch
                }
                if (resp == null) {
                    val error = Throwable("addRealTimeRevisit error,resp is null")
                    error(error.message ?: "")
                    _callingStateFlow.update { CallingState.OnError(error) }
                    _callingStateFlow.update { CallingState.Idle }
                    stopRing()
                    return@launch
                }

                if (resp.isSuccess()) {
                    val data = resp.data ?: run {
                        val error = Throwable("addRealTimeRevisit error,resp.data is null")
                        error(error.message ?: "")
                        _callingStateFlow.update { CallingState.OnError(error) }
                        _callingStateFlow.update { CallingState.Idle }
                        stopRing()
                        return@launch
                    }
                    vodResp = data
                    _callingStateFlow.update { CallingState.Dialing(data) }
                    VodApi.sendOutgoingMsg(
                        OutgoingMsgType.Dialing,
                        request.registeredImId,
                        request.patientName,
                        data
                    ).onFailure { error ->
                        error("Srv:sending dialing msg error", error)
                        hangup(error)
                        stopRing()
                        return@launch
                    }
                } else {
                    val msg = resp.msg
                    if (msg == null) {
                        stopRing()
                        val error = Throwable("addRealTimeRevisit error,code!=200 & msg=null")
                        error(error.message ?: "")
                        _callingStateFlow.update { CallingState.OnError(error) }
                        _callingStateFlow.update { CallingState.Idle }
                        return@launch
                    } else {
                        val regex = Regex(appContext.getString(R.string.queue_num))
                        val matches = regex.find(msg)
                        val number = matches?.groupValues?.getOrNull(1)
                        if (number != null) {
                            trace("Srv: doctor busy! line number:$number")
                            _callingStateFlow.update { CallingState.Busy(runCatching { number.toInt() }.getOrElse { -1 }) }
                            stopRing()
                            _callingStateFlow.update { CallingState.Idle }
                        } else {
                            stopRing()
                            val error = Throwable(msg)
                            error(error.message ?: "Unexcepted addRealTimeRevisit msg:$msg")
                            _callingStateFlow.update { CallingState.OnError(error) }
                            _callingStateFlow.update { CallingState.Idle }
                        }
                    }
                }
            }
        }
    }

    @JvmStatic
    fun hangup(errorTrigger: Throwable? = null) {
        stopRing()
        if (vodResp == null && vodReq == null) {
            _callingStateFlow.update { CallingState.HangedUp }
            _callingStateFlow.update { CallingState.Idle }
            return
        }
        scope.launch {
            VodApi.sendOutgoingMsg(
                OutgoingMsgType.Hangup,
                vodReq?.registeredImId ?: "",
                vodResp?.patientName ?: "",
                vodResp ?: return@launch
            ).fold(onSuccess = {
                trace("Srv: hangup success")
                if (errorTrigger != null) {
                    _callingStateFlow.update { CallingState.OnError(errorTrigger) }
                    _callingStateFlow.update { CallingState.Idle }
                } else {
                    _callingStateFlow.update { CallingState.HangedUp }
                    _callingStateFlow.update { CallingState.Idle }
                }
            }, onFailure = { err ->
                error("Srv: hangup error:${err.message}")
                if (errorTrigger != null) {
                    _callingStateFlow.update { CallingState.OnError(errorTrigger) }
                    _callingStateFlow.update { CallingState.Idle }
                } else {
                    _callingStateFlow.update { CallingState.HangedUp }
                    _callingStateFlow.update { CallingState.Idle }
                }
            })
        }
    }

    @JvmStatic
    fun answer() {
        stopRing()
        val resp = vodResp ?: return
        scope.launch {
            VodApi.sendOutgoingMsg(
                OutgoingMsgType.Answering,
                resp.patientId,
                resp.patientName,
                resp
            ).fold(
                onSuccess = {
                    trace("Srv: answering srv call success")
                    runOnUi { joinInGroupCall(resp) }
                },
                onFailure = { err ->
                    error("Srv: answering srv call failed", err)
                    runOnUi { hangup(err) }
                }
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun joinInGroupCall(resp: VodCallResp?) {
        resp ?: return
        val roomId = TUICommonDefine.RoomId()
        roomId.intRoomId = runCatching { resp.roomId.toInt() }.getOrElse { -1 }
        TUICallKit.createInstance(appContext).joinInGroupCall(
            roomId, resp.conversationId,
            TUICallDefine.MediaType.Video
        )
        trace("Trtc: joining in group call")
        _callingStateFlow.update { CallingState.Connected }
    }

    private fun setCallkitInfo(request: VodCallReq) {
        trace("Trtc:set callkit info:${request}")
        TUICallKit.createInstance(appContext).setSelfInfo(request.patientName, AVATAR, object :
            TUICommonDefine.Callback {
            override fun onSuccess() {
                trace("Trtc:set callkit info success")
            }

            override fun onError(errCode: Int, errMsg: String?) {
                error("Trtc:set callkit info error:(${errCode})$errMsg")
            }
        })
    }

    private fun addIMListener() {
        V2TIMManager.getMessageManager().addAdvancedMsgListener(v2TIMAdvancedMsgListener)
    }

    private fun removeIMListener() {
        V2TIMManager.getMessageManager().removeAdvancedMsgListener(v2TIMAdvancedMsgListener)
    }

    private fun playRing() {
        if (streamID != -1) return
        streamID = soundPool.play(soundId, 1f, 1f, 1, -1, 1f)
    }

    private fun stopRing() {
        if (streamID == -1) return
        soundPool.stop(streamID)
        streamID = -1
    }

    private fun startTargetActivity(context: Context) {
        val intent = Intent(context, VodCallActivity::class.java).apply {
            if (context == context.applicationContext) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
    }

    private fun observerCallState() {
        CallManager.instance.userState.selfUser.get().callStatus.observe(callObserver)
    }

    private var status = TUICallDefine.Status.None
    private val callObserver = Observer<TUICallDefine.Status> {

        if (status != it && it == TUICallDefine.Status.None) {
            trace("Trtc:callStatusObserver, callStatus: $it")
            hangup()
        }
        status = it
    }


    private fun trace(info: String) {
        Log.i("_HomeCareVod", "<${Thread.currentThread().name}>${info}")
    }

    private fun error(message: String, throwable: Throwable? = null) {
        Log.e("_HomeCareVod", "<${Thread.currentThread().name}>${message}", throwable)
    }
}