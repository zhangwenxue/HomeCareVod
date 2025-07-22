package com.homecare.vod.api.cfg

object Configuration {
    var useProdServer = true
    private const val STAGING_BASE_URL = "https://wacdih.shihuahui-health.com.cn"
    private const val PROD_BASE_URL = "https://wacdih.shihuahui-health.com"

    internal fun getBaseUrl(): String {
        return if (useProdServer) PROD_BASE_URL else STAGING_BASE_URL
    }
}