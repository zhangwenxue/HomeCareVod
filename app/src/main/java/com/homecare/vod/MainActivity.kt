package com.homecare.vod

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.homecare.feature.vod.HomeCareVodSDK
import com.homecare.feature.vod.ext.VodSDKConfig
import com.homecare.vod.ui.theme.HomeCareVodTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val config = VodSDKConfig(
            screenOrientation = HomeCareVodSDK.ORIENTATION_PORTRAIT,//屏幕方向，其余选项：ORIENTATION_LANDSCAPE,ORIENTATION_AUTO
            release = false,//设置正式环境
            kickOfflineCallback = { //当前账号被踢下线时的回调

            }
        )
        HomeCareVodSDK.setup(this, config)
        HomeCareVodSDK.login("")
        setContent {
            HomeCareVodTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(modifier = Modifier.padding(innerPadding), onClick = {
//                      Home
                        HomeCareVodSDK.onHangup = {}
                    }) {
                        Text("视频")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HomeCareVodTheme {
        Greeting("Android")
    }
}