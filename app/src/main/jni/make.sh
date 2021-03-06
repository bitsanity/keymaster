#!/bin/bash

# ============================================================================
# script compiles the C code and makes a shared library including it and the
# lib(s) it depends on
# ============================================================================

# HACK: switch to android-16 breaks gcc. workaround is using older version
export NDK="$HOME/Android/android-ndk-r15c"

# Ensure Android NDK exists
[ -z "$NDK" ] && echo "NDK variable must be set" && exit 1

# C does not call into O/S so this is likely unused, but defining it
# in case compiler checks for it and requires it
# using 15 because of the test hardware
export SYSROOT="$NDK/platforms/android-15/arch-arm"
[ ! -d "$SYSROOT" ] && echo "Need SYSROOT: " $SYSROOT && exit 1

# Ensure the android toolchain has been set up
export ANDROID_TC="$HOME/temp/android-toolchain/bin"
echo "Making $ANDROID_TC"
$NDK/build/tools/make-standalone-toolchain.sh \
  --force \
  --arch=arm \
  --platform=android-15 \
  --install-dir=$HOME/temp/android-toolchain

# NOTE: .c and .h files copied from github.com/bitsanity/cryptils project,
#       not generated here

# Ensure path contains the binaries we will need to compile the C stub and
# link its object code to the external shared library
echo $PATH | grep -q "$ANDROID_TC"
[ $? -eq 0 ] && export PATH="$ANDROID_TC:$PATH"

export CSOURCES=\
"a_keymaster_cryptils_Secp256k1.c"

export EXTLIB="libsecp256k1.a"

export CC="$ANDROID_TC/arm-linux-androideabi-gcc --sysroot=$SYSROOT"
export INCLDIRS="-I$HOME/secp256k1/include -I."

export ARCH="armeabi-v7a"
export EXTLDR="./lib/$ARCH/"
export OUTPUT="../jniLibs/$ARCH/libkeymaster.so"
export CFLAGS="-shared -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16"
export LDFLAGS="-march=armv7-a -Wl,--fix-cortex-a8"
export CMD="$CC $CFLAGS $INCLDIRS $CSOURCES -o $OUTPUT $LDFLAGS $EXTLDR$EXTLIB"
echo
echo $CMD
echo
$CMD

export ARCH="armeabi"
export EXTLDR="./lib/$ARCH/"
export OUTPUT="../jniLibs/$ARCH/libkeymaster.so"
export CFLAGS="-shared"
export LDFLAGS=""
export CMD="$CC $CFLAGS $INCLDIRS $CSOURCES -o $OUTPUT $LDFLAGS $EXTLDR$EXTLIB"
echo
echo $CMD
echo
$CMD

# ==========================================================================
# Adding 64-bit version. Android-21 is minimum release level for 64-bit h/w
# ==========================================================================
export SYSROOT="$NDK/platforms/android-21/arch-arm64"
[ ! -d "$SYSROOT" ] && echo "Need SYSROOT: " $SYSROOT && exit 1

echo "Making temp 64-bit toolchain"
$NDK/build/tools/make-standalone-toolchain.sh \
    --force \
    --arch=arm64 \
    --platform=android-21 \
    --install-dir=$HOME/temp/android-toolchain

export ARCH="arm64-v8a"
export EXTLDR="./lib/$ARCH/"
export OUTPUT="../jniLibs/$ARCH/libkeymaster.so"
export CC="$ANDROID_TC/aarch64-linux-android-gcc --sysroot=$SYSROOT"
export CMD="$CC $CFLAGS $INCLDIRS $CSOURCES -o $OUTPUT $LDFLAGS $EXTLDR$EXTLIB"
echo
echo $CMD
echo
$CMD

