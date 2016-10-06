# keymaster

An Android app that implements the role of a "keymaster" in a conversation with
anything in the role of "gatekeeper".

A subsystem of [ADILOS](https://github.com/bitsanity/ADILOS)

## Functions:

1. PIN protection
2. Create a new cryptographic key with a unique name
3. Show public version of key as QR code
4. Show private version of key as BIP38-encrypted content in QR code
5. Use selected key to create a response to a challenge from a "gatekeeper"
7. scan, parse and validate a QR code of a gatekeeper challenge
8. produce and display a QR code of the valid response

## Dependencies:

** Android **
- developed on Ice Cream Sandwich
- expected to work on at least 95% of Android devices

** Java **
- developed on Oracle JDK 1.8 on Ubuntu64
- tested on Samsung Galaxy Nexus

** libsecp256k1 **
- github.com/bitcoin-core/secp256k1
- must be cross-compiled to armeabi

** Scrypt 1.4.0 **
- com.lambdaworks
- github.com/wg/scrypt

** Google's ZXing library **
- github.com/zxing/zxing

## Pros:

1. optical-channel
2. replace physical stuff with virtual keys
3. no network connection needed
4. aproprietary, open-source
5. no third-party required to operate

## Cons:

1. C code called via JNI needs cross-compilation

## Where to Start:

Build the C code (needs Android SDK+NDK)
- download secp256k1 from github
- cross-compile for target platform using NDK
- run: keymaster/app/src/main/jni/make.sh

