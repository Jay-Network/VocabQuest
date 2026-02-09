# VocabQuest ProGuard Rules

# Keep SQLDelight generated code
-keep class com.jworks.vocabquest.db.** { *; }

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.jworks.vocabquest.**$$serializer { *; }
-keepclassmembers class com.jworks.vocabquest.** {
    *** Companion;
}
-keepclasseswithmembers class com.jworks.vocabquest.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor (HTTP client used by Supabase SDK)
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.okhttp.** { *; }

# Supabase SDK
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
-keep class io.github.jan.supabase.gotrue.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.functions.** { *; }

# OkHttp (used by Ktor engine)
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Suppress SLF4J warnings (used by Ktor/Supabase)
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Keep Supabase auth request/response models
-keep class * implements kotlinx.serialization.KSerializer { *; }
