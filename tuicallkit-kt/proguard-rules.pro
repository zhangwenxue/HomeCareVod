# 已有规则
-keep class com.tencent.** { *; }

# 添加以下规则
# 保留Gson相关
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# 保留TRTC SDK相关
-keep class io.trtc.** { *; }

# 保留反射用到的类
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# 保留序列化相关
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

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

-dontwarn java.lang.invoke.StringConcatFactory