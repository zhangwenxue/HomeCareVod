package com.homecare.feature.vod.ext

import android.content.Context
import android.os.PowerManager


object WakeLockUtil {

    fun acquireWakeLock(context: Context, timeout: Long): PowerManager.WakeLock? {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ON_AFTER_RELEASE,
            context.javaClass.name
        )
        wakeLock.acquire(timeout)
        return wakeLock
    }

    fun release(wakeLock: PowerManager.WakeLock?) {
        if (wakeLock != null && wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}