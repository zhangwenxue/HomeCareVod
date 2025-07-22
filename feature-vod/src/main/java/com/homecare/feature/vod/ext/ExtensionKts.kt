package com.homecare.feature.vod.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun CoroutineScope.runOnUi(action: () -> Unit) {
    this.launch(Dispatchers.Main) { action() }
}

val Result<*>.errorMessage: String
    get() {
        return this.exceptionOrNull()?.message ?: "Known error"
    }