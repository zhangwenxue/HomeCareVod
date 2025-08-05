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
                        HomeCareVodSDK.call(
                            token = "14E9FA067B350997836938946055A9BCA483740CC54EB29E6642D065CADCF5D8",
                            "1848207774300024832",
                            "1848207774300024832",
                            symptomsDescription = "头疼",
                            fileList = listOf(
                                "https://upload-images.jianshu.io/upload_images/5809200-48dd99da471ffa3f.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240",
                                "https://upload-images.jianshu.io/upload_images/5809200-7fe8c323e533f656.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240"
                            ),
                            true,

                        )

//                        HomeCareVodSDK.callWithJwt(
//                            jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJoYWllclJvYm90IiwibmFtZSI6Iua1t-WwlOacuuWZqOS6uiIsImlhdCI6MTc1MzE2NTY1OX0.Xr_othyi9HBMoOnrr758VG6PCwXZ7jX24jxvpVUuxJE",
//                            "1848207774300024832",
//                            "1848207774300024832",
//                            symptomsDescription = "测试症状描述",
//                            fileList = listOf(
//                                "http://e.hiphotos.baidu.com/image/pic/item/a1ec08fa513d2697e542494057fbb2fb4316d81e.jpg",
//                                "http://c.hiphotos.baidu.com/image/pic/item/30adcbef76094b36de8a2fe5a1cc7cd98d109d99.jpg"
//                            ), hasEcg = true,
//                            onHangup = {
//                                Toast.makeText(
//                                    this@MainActivity,
//                                    "挂断成功:$it",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        )
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