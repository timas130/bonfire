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

-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString

# required for reflection in devsup Json
-keep class ** {
    public static *** instance(com.sup.dev.java.libs.json.Json);
}
-keepclassmembernames class ** {
    public static *** instance(com.sup.dev.java.libs.json.Json);
}

-keepclassmembers class ** extends com.sup.dev.java.libs.json.JsonParsable {
    <init>();
}

# required for reflection in API_TRANSLATE
-keepclassmembers class com.dzen.campfire.api.API_TRANSLATE { *; }
-keepclassmembernames class com.dzen.campfire.api.API_TRANSLATE { *; }

# for sending requests and such
-keepnames class com.dzen.campfire.api.**
-keepnames class com.dzen.campfire.api_media.**

# for analytics
-keepnames class ** extends com.sup.dev.android.libs.screens.Screen

# for ndk
-keepclasseswithmembers class com.waynejo.androidndkgif.** { *; }
-keepclasseswithmembernames class com.waynejo.androidndkgif.** { *; }
