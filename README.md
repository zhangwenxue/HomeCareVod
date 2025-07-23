# 家健康视频医生SDK

## SDK使用

### 1.引入SDK
> **Gradle(Kotlin)**
<br>implementation("io.github.zhangwenxue:homecarevod:0.0.2")
<br>
<br>
> **Gradle(Short)**
<br>implementation 'io.github.zhangwenxue:homecarevod:0.0.2'

### 2.配置Android 工程
在 app目录下找到AndroidManifest.xml 文件，在 application 节点中添加 tools:replace="android:allowBackup" ，覆盖组件内的设置，使用自己的设置。
<br>

```
  // app/src/main/AndroidManifest.xml
  <application
    android:name=".DemoApplication"
    android:allowBackup="false"
    android:icon="@drawable/app_ic_launcher"
    android:label="@string/app_name"
    android:largeHeap="true"
    android:theme="@style/AppTheme"
    tools:replace="android:allowBackup">
```

### 3.初始化SDK

```
val config = VodSDKConfig(
            //屏幕方向，可选项:ORIENTATION_PORTRAIT,ORIENTATION_LANDSCAPE,ORIENTATION_AUTO
            screenOrientation = HomeCareVodSDK.ORIENTATION_PORTRAIT,
            release = true,//设置正式环境
            kickOfflineCallback = { 
                //当前账号被踢下线时的回调，如果想继续监听医生回拨时间，需要执行第四步login操作
                // HomeCareVodSDK.login()
            }
        )
// 初始化SDK        
HomeCareVodSDK.setup(context, config)
```

### 4.登录注册账号ID（建议及早登录，登录后支持医生回拨）
```
HomeCareVodSDK.login(userId)
```

### 5.发起视频医生问诊
```
HomeCareVodSDK.callWithJwt(
        jwtToken = "jwt token",//必要参数
        "注册账号Id",//必要参数
        "就诊患者Id",//必要参数
        symptomsDescription = "症状描述",//可选
        fileList = listOf(),//可选，问诊附件列表
        hasEcg = false//可选，标识是否有心电图数据
    )
```