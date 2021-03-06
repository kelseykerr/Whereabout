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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keepclassmembers class com.kelseykerr.whereabout.models.** { *; }

-keep class com.kelseykerr.whereabout.models.** { *; }

-keep class kotlin.Metadata { *; }

-keepclassmembers public class com.kelseykerr.** {
    public synthetic <methods>;
}

-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}

-keepclassmembers class com.wirelessregistry.observersdk.events.EventService

-keep class com.fasterxml.jackson.annotation.** { *; }

-dontwarn javax.annotation.Nullable

-dontwarn javax.annotation.Nonnull

-keepclassmembers class com.wirelessregistry.pcap.** { *; }

-keep class com.wirelessregistry.pcap.** { *; }

-dontwarn com.fasterxml.jackson.databind.**

-dontwarn com.wirelessregistry.pcap.**

-dontwarn kotlin.reflect.jvm.internal.**


