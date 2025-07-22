package com.homecare.vod

import android.os.Bundle
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
        HomeCareVodSDK.setup(this, VodSDKConfig(release = false) {})
        HomeCareVodSDK.login("1848207774300024832")
        setContent {
            HomeCareVodTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Button(modifier = Modifier.padding(innerPadding), onClick = {
//                        HomeCareVodSDK.dial(
//                            token = "14E9FA067B350997836938946055A9BCA483740CC54EB29E6642D065CADCF5D8",
//                            "1848207774300024832",
//                            "1848207774300024832"
//                        )
                        HomeCareVodSDK.callWithJwt(
                            jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJoYWllclJvYm90IiwibmFtZSI6Iua1t-WwlOacuuWZqOS6uiIsImlhdCI6MTc1MzE2NTY1OX0.Xr_othyi9HBMoOnrr758VG6PCwXZ7jX24jxvpVUuxJE",
                            "1848207774300024832",
                            "1848207774300024832"
                        )
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