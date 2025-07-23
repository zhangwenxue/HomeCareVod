# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.homecare.vod.api.service.remote.VodApi
-keep class com.homecare.vod.api.cfg.** { *; }
-keep class com.homecare.vod.api.entity.** { *; }
# 保留协程状态机相关代码
-keepclassmembers class kotlin.coroutines.jvm.internal.** {
    *;
}

# 保留挂起函数签名
-keepclassmembers class * {
    kotlin.coroutines.Continuation suspend*(...);
}


# Keep Kotlin object INSTANCE fields
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin object classes and their companion objects
-keepclassmembers class **$Companion {
    *;
}

-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keepclassmembers class * {
    static final *** INSTANCE;
}
# Keep VodApi class and all its methods
-keep class com.homecare.vod.api.service.remote.VodApi {
    *;
}

# Keep Kotlin suspend function signatures
-keepclassmembers class * {
    kotlin.coroutines.Continuation suspend*(...); 
}

# Keep Kotlin coroutines implementation details
-keepclassmembers class kotlin.coroutines.jvm.internal.** {
    *;
}

# Keep specific method name patterns that match Kotlin's naming conventions for suspend functions
-keepclassmembers,allowshrinking,allowobfuscation class ** {
    java.lang.Object *-*(java.lang.Object, kotlin.coroutines.Continuation);
}