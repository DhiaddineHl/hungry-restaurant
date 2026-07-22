# Keep Sunmi AIDL service interfaces.
-keep class woyou.aidlservice.jiuiv5.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.hungry.restaurant.pos.data.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
