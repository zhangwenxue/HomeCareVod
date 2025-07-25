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

-keep class com.tencent.** { *; }
# Keep HomeCareVodSDK and related classes
-keep class com.homecare.** { *; }

# Keep any classes that might be accessed via reflection
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature

# Keep Tencent SDK classes
-keep class com.tencent.** { *; }
-keep class io.trtc.** { *; }

# Keep Gson related classes
-keep class com.google.gson.** { *; }

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Fix for Java 9+ StringConcatFactory
-dontwarn java.lang.invoke.StringConcatFactory

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