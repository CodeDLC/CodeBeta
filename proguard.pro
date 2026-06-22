# -injars input.jar
# -outjar obf.jar

-repackageclasses 'Nursultan'
-allowaccessmodification

-keep public class fun.vegax.VegaXDLC {
    public static void main(java.lang.String[]);
}

-keepclassmembers class * {
    @com.google.common.eventbus.Subscribe <methods>;
}

-keep class * extends java.lang.annotation.Annotation { *; }

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes *Annotation*,Signature,EnclosingMethod,InnerClasses,LineNumberTable,MethodParameters,SourceFile,Exceptions

-keep,allowobfuscation @interface fun.vegax.mixins.** { *; }
-keep class fun.vegax.mixins.** { *; }
-keep @interface org.spongepowered.asm.mixin.** { *; }
-keep class org.spongepowered.asm.mixin.** { *; }

-keep class **.factory.** { *; }
-keep class **.Factory { *; }
-keep class **Factory { *; }
-keep interface **Factory { *; }

-dontwarn