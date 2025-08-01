package com.homecare.feature.vod.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.homecare.feature.vod.HomeCareVodSDK
import com.homecare.feature.vod.R
import com.homecare.feature.vod.databinding.ActivityVodCallBinding
import com.homecare.feature.vod.ext.CallingState
import com.homecare.feature.vod.ext.LoginState
import com.trtc.tuikit.common.util.ToastUtil
import kotlinx.coroutines.launch

class VodCallActivity : ComponentActivity() {
    companion object {
        private const val AVATAR_PLACEHOLDER_URL =
            "https://wehealth-oss-public.oss-cn-beijing.aliyuncs.com/prod/assets/video_doctor_wait_bg.png"
    }

    private val binding by lazy { ActivityVodCallBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContentView(binding.root)

        Glide.with(binding.doctorAvatarPlaceholder)
            .load("${AVATAR_PLACEHOLDER_URL}?timestamp=${System.currentTimeMillis()}")
            .placeholder(R.drawable.doctor_avatar_placeholder)
            .into(binding.doctorAvatarPlaceholder)

        setupEvents()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                HomeCareVodSDK.callingStateFlow.collect {
                    handleCallingState(it)
                }

            }
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                HomeCareVodSDK.loginStateFlow.collect {
                    handleLoginState(it)
                }
            }
        }
    }

    private fun setupEvents() {
        binding.hangup.setOnClickListener { HomeCareVodSDK.hangup() }
        binding.answerDoctorCall.setOnClickListener { HomeCareVodSDK.answer() }
        binding.hangupDoctorCall.setOnClickListener { HomeCareVodSDK.hangup() }
    }

    private fun handleLoginState(state: LoginState) {
        when (state) {
            is LoginState.ConnectFailed -> {
                toast("登录成功")
            }

            is LoginState.ConnectSuccess -> {}
            is LoginState.Connecting -> {
                toast("账号正在登录，请稍后")
            }

            LoginState.Idle -> {}

            is LoginState.KickedOffline -> {
                toast("账号已经在其他设备登录")
            }

            is LoginState.LoggedOut -> {}
            is LoginState.UserSigExpired -> {
                toast("正在重新登录，请稍后")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleCallingState(state: CallingState) {
        when (state) {
            is CallingState.Busy -> {
                AlertDialog.Builder(this)
                    .setTitle("医生正在接诊，请稍后")
                    .setMessage("您的排队号为：${state.lineNumber}")
                    .setNegativeButton(
                        "我知道了"
                    ) { dialog, which ->
                        finish()
                        dialog.dismiss()
                    }.create()
                    .show()
            }

            is CallingState.Dialing -> {
                binding.answeringGroup.isVisible = false
                binding.dialingGroup.isVisible = true
                state.response?.let {
                    binding.doctorAvatarPlaceholder.isVisible = false
                    it.doctorHeadImg?.let { url ->
                        val glideUrl = GlideUrl(
                            url,
                            LazyHeaders.Builder()
                                .addHeader("referer", "https://wacdih.shihuahui-health.com/")
                                .build()
                        )
                        Glide.with(binding.doctorAvatar)
                            .load(glideUrl)
                            .into(binding.doctorAvatar)
                    }

                    binding.doctorName.text = "${it.doctorName}${it.titleName ?: ""}"
                }
            }

            CallingState.Connected -> {
                // toast("通话已接通")
                finish()
            }

            CallingState.HangedUp -> {
                toast("已挂断")
                finish()
            }

            CallingState.Idle -> {

            }

            is CallingState.Answering -> {
                binding.answeringGroup.isVisible = true
                binding.dialingGroup.isVisible = false
                binding.doctorAvatarPlaceholder.isVisible = false
                Glide.with(binding.doctorAvatar)
                    .load(state.response?.doctorHeadImg)
                    .transition(withCrossFade())
                    .into(binding.doctorAvatar)
                binding.callingTip.text = String.format(
                    resources.getString(R.string.doctor_calling),
                    state.response?.doctorName ?: ""
                )
                binding.doctorName.text =
                    "${state.response?.doctorName ?: ""}${state.response?.titleName ?: ""}"
            }

            is CallingState.OnError -> {
                toast(state.error.message ?: "通话异常")
                finish()
            }

            CallingState.Rejected -> {
                toast("医生正忙，请稍后重试")
                finish()
            }

            CallingState.SessionEnd -> {
                toast("问诊已结束,感谢您的使用")
                // finish()
            }
        }
    }

    override fun onBackPressed() {}

    private fun toast(msg: String) {
        ToastUtil.show(msg, false, Gravity.CENTER)
    }
}