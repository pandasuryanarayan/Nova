# ProGuard / R8 configuration for Nova Browser
-keepattributes JavascriptInterface,RuntimeVisibleAnnotations

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.example.browser.NovaJsBridge { *; }
-keep class com.example.browser.NovaDevToolsJsBridge { *; }

-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(java.lang.Object);
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    public static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-assumevalues class kotlinx.coroutines.DebugKt {
    public static final boolean getDEBUG() return false;
    public static final boolean getASSERTIONS_ENABLED() return false;
    public static final boolean getRECOVER_STACK_TRACES() return false;
}

-repackageclasses
-allowaccessmodification
