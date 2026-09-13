# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# kotlinx.serialization: keep serializer() on the companion of every @Serializable class so R8
# doesn't strip the generated KSerializer accessed via reflection at the Json.decodeFromString
# call site.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclasseswithmembers class com.ankitt.themovieshow.**$$serializer {
    *** serializer(...);
}
-keepclassmembers class com.ankitt.themovieshow.** {
    *** Companion;
}
-keepclasseswithmembers class com.ankitt.themovieshow.** {
    kotlinx.serialization.KSerializer serializer(...);
}
