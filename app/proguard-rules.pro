## GardenFlow release hardening
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

## Hilt / Room generated classes are referenced reflectively by Android tooling.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class **_*Dao_Impl { *; }
-keep class **_*Database_Impl { *; }

## Kotlin serialization DTOs.
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class com.tony.gardenflow.data.remote.deepseek.** { *; }
-keep class com.tony.gardenflow.data.remote.weather.** { *; }
-keep class com.tony.gardenflow.domain.model.** { *; }

## Google Play / ML Kit / CameraX public APIs.
-keep class com.google.android.play.core.integrity.** { *; }
-keep class com.google.mlkit.** { *; }
-keep class androidx.camera.** { *; }

## OkHttp and coroutines warnings that are safe for Android release builds.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
