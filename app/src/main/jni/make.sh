#!/bin/bash

# ============================================================================
# script compiles the C code and makes a shared library including it and the
# lib(s) it depends on
#
# needs the .h file produced by compiling the native class declaration
#
# ============================================================================

# Ensure Android NDK exists
[ -z "$NDK" ] && echo "NDK variable must be set" && exit 1

# C does not call into O/S so this is likely unused, but defining it
# in case compiler checks for it and requires it
# using 15 because of the test hardware
export SYSROOT="$NDK/platforms/android-15/arch-arm"
[ ! -d "$SYSROOT" ] && echo "Need SYSROOT: " $SYSROOT && exit 1

# Ensure the android toolchain has been set up
export ANDROID_TC="/tmp/android-toolchain/bin"
[ ! -d "$ANDROID_TC" ] && echo "Need $ANDROID_TC" && exit 1

# Ensure path contains the binaries we will need to compile the C stub and
# link its object code to the external shared library
echo $PATH | grep -q "$ANDROID_TC"
[ $? -eq 0 ] && export PATH="$ANDROID_TC:$PATH"

export CSOURCES="a_keymaster_cryptils_Secp256k1.c"
export EXTLIB="libsecp256k1.a"

export CC="$ANDROID_TC/arm-linux-androideabi-gcc --sysroot=$SYSROOT"
export INCLDIRS="-I/home/skull/secp256k1/include -I."

export ARCH="armeabi-v7a"
export EXTLDR="./lib/$ARCH/"
export OUTPUT="../jniLibs/$ARCH/libkeymaster.so"
export CFLAGS="-shared -march=armv7-a -mfloat-abi=softfp -mfpu=vfpv3-d16"
export LDFLAGS="-march=armv7-a -Wl,--fix-cortex-a8"
export CMD="$CC $CFLAGS $INCLDIRS $CSOURCES -o $OUTPUT $LDFLAGS $EXTLDR$EXTLIB"
echo
echo $CMD
echo
#$CMD

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

