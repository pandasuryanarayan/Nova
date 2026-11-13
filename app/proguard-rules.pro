# ProGuard / R8 configuration for Nova Browser
# Keep WebView JavascriptInterface bridges so R8 minification never strips JS-to-Android methods
-keepattributes JavascriptInterface
-keepattributes *Annotation*

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.example.browser.NovaJsBridge { *; }
-keep class com.example.browser.NovaDevToolsJsBridge { *; }

# Keep Room database and entities while allowing R8 field/method shrinking
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-repackageclasses
-allowaccessmodification
