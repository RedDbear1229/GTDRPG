# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.questlog.**$$serializer { *; }
-keepclassmembers class com.questlog.** { *** Companion; }
-keepclasseswithmembers class com.questlog.** { kotlinx.serialization.KSerializer serializer(...); }

# Retrofit
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
