-keep class fun.vegax.** { *; }
-keep class antidaunleak.** { *; }
-keep @antidaunleak.api.annotation.Native class * { *; }

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keep class net.minecraft.** { *; }
-keep class net.fabricmc.** { *; }

-dontwarn net.minecraft.**
-dontwarn net.fabricmc.**

-keep class com.mojang.** { *; }
-dontwarn com.mojang.**

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-adaptclassstrings
