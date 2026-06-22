# Fabric mod ProGuard rules
-keep class net.fabricmc.loader.** { *; }
-keep class net.fabricmc.api.** { *; }
-keep interface net.fabricmc.api.** { *; }

# Keep mixin classes and methods
-keep @net.fabricmc.api.Environment class * { *; }
-keep @net.fabricmc.api.EnvType class * { *; }
-keep class * implements net.fabricmc.api.ModInitializer { *; }
-keep class * implements net.fabricmc.api.ClientModInitializer { *; }
-keep class * implements net.fabricmc.api.DedicatedServerModInitializer { *; }

# Keep mixin methods
-keep class * {
    @net.fabricmc.api.Environment *;
    @net.fabricmc.api.EnvType *;
}

# Keep access widener
-keep class * { *; }

# Don't optimize mixin classes
-keep class net.minecraft.** { *; }
-keep class com.mojang.** { *; }

# Keep Fabric API
-keep class net.fabricmc.fabric.** { *; }
-keep interface net.fabricmc.fabric.** { *; }

# Keep Lombok generated code
-keep class lombok.** { *; }
-dontwarn lombok.**

# Keep Kotlin
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# Keep Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep Caffeine
-keep class com.github.benmanes.** { *; }
-dontwarn com.github.benmanes.**

# Keep OSHI
-keep class oshi.** { *; }
-dontwarn oshi.**

# Keep WebSocket
-keep class org.java_websocket.** { *; }
-dontwarn org.java_websocket.**
