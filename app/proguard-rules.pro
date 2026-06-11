# QueueFuel R8/ProGuard rules.
#
# Library-specific keep rules (Room, OkHttp, Coil, Compose, Firebase) ship as
# consumer rules inside each AAR/JAR — only app-specific tweaks belong here.

# Keep readable stack traces in crash reports while still obfuscating names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OkHttp platform abstractions reference optional JVM-only classes.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
