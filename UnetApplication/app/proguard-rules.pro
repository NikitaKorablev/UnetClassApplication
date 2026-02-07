# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# PyTorch Android library - keep all classes and methods from being obfuscated
-keep class org.pytorch.** { *; }
-keep class org.pytorch.native.** { *; }
-dontwarn org.pytorch.**
-keep class ai.djl.pytorch.** { *; }
-dontwarn ai.djl.pytorch.**

# Keep native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Keep JNI references
-keep class * {
    *** invoke(long, java.lang.Object[]);
}

# Preserve native libraries from obfuscation
-keep class * {
    native <methods>;
}

# Keep PyTorch native methods specifically
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep PyTorch model loading functionality
-keep class * implements org.pytorch.IValue
-keep class * implements org.pytorch.Module
-keep class * implements org.pytorch.Tensor

# Prevent shrinking of PyTorch classes
-keepnames class org.pytorch.**
-keepclassmembers enum org.pytorch.** {
    *;
}

# Specific rules for PyTorch Android native initialization
-keep class org.pytorch.PyTorchAndroid { *; }
-keep class org.pytorch.LiteNativePeer { *; }
-keep class org.pytorch.NativePeer { *; }
-keep class org.pytorch.PyTorchException { *; }

# Keep native library loading methods
-keepclassmembers class * {
    native <methods>;
}

# Keep PyTorch native library names from being obfuscated
-keep class * {
    boolean nativeModuleRegister();
    boolean nativeModuleUnregister();
}

# Don't optimize PyTorch classes
-optimizations !method/inlining/*
-keep class org.pytorch.PyTorchAndroid { *; }
-dontoptimize

# More specific JNI rules for PyTorch
-keep class org.pytorch.** { *; }
-keep interface org.pytorch.** { *; }
-keep class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    native <methods>;
}
-keepclassmembers class * {
    native <methods>;
}

# Keep PyTorch native libraries from being stripped
-keep class org.pytorch.Native { *; }
-keep class org.pytorch.BuildConfig { *; }
-keep class org.pytorch.opencv.** { *; }
-dontwarn org.pytorch.opencv.**

# Keep Facebook JNI classes and fields intact
-keep class com.facebook.jni.** { *; }
-keep class com.facebook.jni.HybridData$Destructor { *; }
-keep class com.facebook.jni.HybridData { *; }
-keep class com.facebook.jni.HybridData$Destructor {
    private com.facebook.jni.HybridData$Destructor mDestructor;
}
-keepclasseswithmembers class com.facebook.jni.HybridData {
    private com.facebook.jni.HybridData$Destructor mDestructor;
}

# Keep fbjni native methods
-keep class * {
    native <methods>;
}
-keepclasseswithmembers class * {
    native <methods>;
}
-keepclassmembers class * {
    native <methods>;
}

# Specific rule for HybridData Destructor field
-keepclassmembers class com.facebook.jni.HybridData {
    com.facebook.jni.HybridData$Destructor mDestructor;
}

# Keep NativePeer and related classes
-keep class org.pytorch.NativePeer { *; }
-keep class org.pytorch.NativePeer$NativePeerDeallocator { *; }
-keep class * extends org.pytorch.NativePeer { *; }

# Keep native methods of NativePeer
-keepclassmembers class org.pytorch.NativePeer {
    native <methods>;
}

# Add missing rules from R8
-dontwarn javax.annotation.Nullable