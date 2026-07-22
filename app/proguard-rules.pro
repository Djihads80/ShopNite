# No project-specific rules yet.
-keep,allowobfuscation,allowshrinking class com.google.firebase.messaging.**

# Keep serializable classes and their members for kotlinx.serialization
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Transient <fields>;
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **$$serializer { *; }

# Keep OkHttp and Okio for Retrofit and Coil
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
