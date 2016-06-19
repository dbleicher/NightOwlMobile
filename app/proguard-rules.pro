# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

-dontwarn com.squareup.okhttp.**
-dontwarn com.squareup.picasso.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.internal.**

-keep,includedescriptorclasses public class * extends android.app.Activity
-keep,includedescriptorclasses public class * extends android.app.Application
-keep,includedescriptorclasses public class * extends android.app.Service
-keep,includedescriptorclasses public class * extends android.content.BroadcastReceiver
-keep,includedescriptorclasses public class * extends android.content.ContentProvider

## Event Bus
-keepclassmembers,includedescriptorclasses class ** { public void onEvent*(**); }

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
-keepattributes InnerClasses

# Gson specific classes
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep,includedescriptorclasses class com.geofinity.wgu.nightowl.model.** { *; }

#Public Web interfaces DOH!
-keep,includedescriptorclasses class com.geofinity.wgu.nightowl.netops.CosProgressInterface { *; }
-keep,includedescriptorclasses class com.geofinity.pwnet.models.PanoVideo { *; }

-keep,includedescriptorclasses class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

-keep,includedescriptorclasses class org.jsoup.** { *; }
-keep interface org.jsoup.** { *; }

-keep,includedescriptorclasses public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers,includedescriptorclasses class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers,includedescriptorclasses class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers,includedescriptorclasses class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers,includedescriptorclasses class **.R$* {
    public static <fields>;
}
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
