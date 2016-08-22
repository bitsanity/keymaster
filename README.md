# keymaster

An Android app that implements the role of a "keymaster" in a conversation with
anything in the role of "gatekeeper".

A subsystem of [ADILOS](https://github.com/bitsanity/ADILOS)

## Functions:

1. Create and confirm a PIN
2. Create a new highly-random cryptographic key
3. Assign unique name to new key
4. Show public version of key as QR code
5. Show private version of key as BIP38-encrypted content in QR code
6. Use selected key to create a digitally signed response to a challenge
   from a "gatekeeper"
7. scan, parse and validate a QR code of a gatekeeper challenge
8. produce a QR code of the valid result to the challenge

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

** Bouncy Castle **
- TODO: replace with rmd160.c and remove dependency
- for RIPEMD160
- www.bouncycastle.org/java.html

** Google's ZXing library **
- github.com/zxing/zxing

## Pros:

1. Yields optical-channel digital sign-in to gatekeeper devices
2. Replaces physical keys, magnetic keycards, swipe-cards, debit/credit cards
3. Requires no network - smart phone can be used even without cell or WiFi
4. Aproprietary and open-source
5. Does not require third-party/pay service

## Cons:

1. Contains C code called via JNI, requiring additional dev work

## Where to Start:

Build the C code (needs Android SDK+NDK)
- download/build secp256k1 from github
- look into: keymaster/app/src/main/jni/make.sh

Import keymaster filetree to Android Studio and build APK:
- volunteer to undergo full tax audit
- beat self in head until fist is bloody mess, change hands
- pursue PhD in 18th century Chinese Opera
- all more appealing than sorting out Android build/JNI process although Studio
  pretty good overall, really

