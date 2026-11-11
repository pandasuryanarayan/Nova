# ProGuard / R8 configuration for Nova Browser
# Keep WebView JavascriptInterface bridges so R8 minification never strips JS-to-Android methods
-keepattributes JavascriptInterface
-keepattributes *Annotation*

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.example.browser.NovaJsBridge { *; }
-keep class com.example.browser.NovaDevToolsJsBridge { *; }
-keep class com.example.data.** { *; }
-keep class com.example.vpn.** { *; }

# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# OkHttp / Kotlinx Coroutines warnings
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
