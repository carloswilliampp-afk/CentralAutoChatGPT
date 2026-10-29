# Add project specific ProGuard rules here.
-keep class com.carlauncher.auto.** { *; }
-keepclassmembers class * {
    @androidx.room.* *;
}
-dontwarn org.osmdroid.**
