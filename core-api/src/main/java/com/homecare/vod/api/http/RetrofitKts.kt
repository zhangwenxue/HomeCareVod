package com.homecare.vod.api.http

import android.annotation.SuppressLint
import android.util.Log
import com.homecare.vod.api.cfg.Configuration.getBaseUrl
import com.homecare.vod.api.entity.ApiResp
import com.homecare.vod.api.entity.toResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

suspend fun <T> withHttpResult(request: suspend () -> ApiResp<T>): Result<T> {
    return withContext(Dispatchers.IO) {
        val resp = runCatching {
            val ret = request().toResult()
            val exception = ret.exceptionOrNull()
            if (exception != null) throw exception
            val data = ret.getOrNull()
            if (data == null) throw NullPointerException("Server response data is null")
            data
        }.onFailure {
            Log.i("ServerErr", "服务异常", it)
        }
        resp
    }
}

internal inline fun <reified S> retrofitService(
    baseUrlProducer: () -> String = { getBaseUrl() },
    enableLog: Boolean = true,
    factory: Converter.Factory = GsonConverterFactory.create(),
    vararg interceptors: Interceptor? = emptyArray()
): S = Retrofit.Builder()
    .baseUrl(baseUrlProducer())
    .addConverterFactory(factory)
    .client(okHttpClient(enableLog, *interceptors))
    .build()
    .create(S::class.java)

internal fun okHttpClient(
    enableLog: Boolean = true,
    vararg interceptors: Interceptor? = emptyArray()
) = OkHttpClient.Builder().apply {
    if (enableLog) {
        addInterceptor(HttpLoggingInterceptor {
            Log.i("_HttpRequest", it)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }
    interceptors.filterNotNull().forEach {
        addInterceptor(it)
    }
}.apply {
    val factory = SSLContext.getInstance("SSL").let {
        it.init(null, trustedCerts, SecureRandom())
        it.socketFactory
    }
    sslSocketFactory(factory, trustedCerts[0] as X509TrustManager)
    hostnameVerifier { _, _ -> true }
}.retryOnConnectionFailure(true)
    .build()

private val trustedCerts = arrayOf<TrustManager>(
    @SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }

    })