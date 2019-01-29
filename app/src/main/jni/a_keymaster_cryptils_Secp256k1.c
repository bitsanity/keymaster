#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "secp256k1.h"
#include "a_keymaster_cryptils_Secp256k1.h"

// constants - sizes in bytes
#define SEEDLEN 32
#define TWKSIZE 32
#define PVTKEYSZ 32
#define PUBKEYSZ 33
#define UNCOMPPUBKEYSZ 65
#define SIGLEN 65

int compressed = SECP256K1_EC_COMPRESSED;
secp256k1_context* pCONTEXT = NULL;

// -------------------
// int resetContext();
// -------------------

JNIEXPORT jint JNICALL Java_a_keymaster_cryptils_Secp256k1_resetContext( JNIEnv * env, jobject obj )
{
  if ( NULL == pCONTEXT )
    pCONTEXT = secp256k1_context_create( SECP256K1_CONTEXT_VERIFY |
                                         SECP256K1_CONTEXT_SIGN );

  srand( time(NULL) );
  unsigned char seed32[SEEDLEN];

  int ii;
  for (ii = 0; ii < SEEDLEN; ii++)
  {
    int nextrnd = rand();
    unsigned char b = nextrnd & (unsigned char)0xFF;
    seed32[ii] = b;
  }

  return (jint)secp256k1_context_randomize( pCONTEXT, seed32 );
}

// -----------------------------------------------
//  boolean privateKeyIsValid( byte[] in_seckey );
// -----------------------------------------------

JNIEXPORT jboolean JNICALL Java_a_keymaster_cryptils_Secp256k1_privateKeyIsValid
  ( JNIEnv * env, jobject obj, jbyteArray in_seckey )
{
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len )
    return JNI_FALSE;

  // sub returns 1 if valid, 0 if invalid
  int res = secp256k1_ec_seckey_verify( pCONTEXT, jkey );

  if (1 == res) return JNI_TRUE;
  return JNI_FALSE;
}

// --------------------------------------------
//  byte[] publicKeyCreate( byte[] in_seckey );
// --------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_publicKeyCreate
  ( JNIEnv * env, jobject obj, jbyteArray in_seckey )
{
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;

  unsigned char * seckeybytes = (unsigned char *)jkey;

  secp256k1_pubkey pubkey;

  if ( 1 != secp256k1_ec_pubkey_create(pCONTEXT, &pubkey, seckeybytes) )
    return NULL;

  // pubkey struct is opaque - call serialize function to get a byte array
  unsigned char serialPubKey[ PUBKEYSZ ];
  size_t outLen = (size_t)PUBKEYSZ;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );

  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}

// -----------------------------------------------
// byte[] uncompressPublicKey( byte[] in_pubkey );
// -----------------------------------------------
JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_uncompressPublicKey
  ( JNIEnv * env, jobject obj, jbyteArray in_pubkey )
{
  // convert pubkey from serialized to opaque form
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_pubkey );
  if ( NULL == jkey ) return NULL;
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  secp256k1_pubkey pubkey;
  if ( 1 != secp256k1_ec_pubkey_parse(
              pCONTEXT, &pubkey, pubkeybytes, (size_t)len) )
    return NULL;

  // serialize opaque result to uncompressed size
  unsigned char serialPubKey[ UNCOMPPUBKEYSZ ];
  size_t outLen = (size_t)UNCOMPPUBKEYSZ;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       SECP256K1_EC_UNCOMPRESSED );
  // convert result from C to java
  jbyteArray result = (*env)->NewByteArray( env, UNCOMPPUBKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, UNCOMPPUBKEYSZ, serialPubKey );

  return result;
}

// -----------------------------------------------------------
//  byte[] privateKeyAdd( byte[] in_seckey, byte[] in_tweak );
// -----------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_privateKeyAdd
  (JNIEnv * env, jobject obj, jbyteArray in_seckey, jbyteArray in_tweak )
{
  // java to C - seckey

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;

  unsigned char * seckeybytes = (unsigned char *)jkey;
  unsigned char seckey[ PVTKEYSZ ];
  memcpy( (void *)seckey, (void *)seckeybytes, PVTKEYSZ );

  // java to C - tweak

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // do operation

  if (1 != secp256k1_ec_privkey_tweak_add(pCONTEXT, seckey, tweakbytes) ) return NULL;

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, PVTKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PVTKEYSZ, seckey );

  return result;
}

// ------------------------------------------------------------
//  byte[] privateKeyMult( byte[] in_seckey, byte[] in_tweak );
// ------------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_privateKeyMult
  ( JNIEnv * env, jobject obj, jbyteArray in_seckey, jbyteArray in_tweak )
{
  // java to C - seckey

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;

  unsigned char * seckeybytes = (unsigned char *)jkey;
  unsigned char seckey[ PVTKEYSZ ];
  memcpy( (void *)seckey, (void *)seckeybytes, PVTKEYSZ );

  // java to C - tweak

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // do operation

  if (1 != secp256k1_ec_privkey_tweak_mul(pCONTEXT, seckey, tweakbytes) ) return NULL;

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, PVTKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PVTKEYSZ, seckey );

  return result;
}

// ----------------------------------------------------------
//  byte[] publicKeyAdd( byte[] in_pubkey, byte[] in_tweak );
// ----------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_publicKeyAdd
  (JNIEnv * env, jobject obj, jbyteArray in_pubkey, jbyteArray in_tweak )
{
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_pubkey );
  if ( NULL == jkey ) return NULL;
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // convert pubkey from serialized to opaque form

  secp256k1_pubkey pubkey;
  if ( 1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, (size_t)len) )
    return NULL;

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // perform the operation

  if (1 != secp256k1_ec_pubkey_tweak_add(pCONTEXT, &pubkey, tweakbytes) ) return NULL;

  // serialize opaque result

  unsigned char serialPubKey[ PUBKEYSZ ];
  size_t outLen = (size_t)PUBKEYSZ;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  // convert result from C to java

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}

// -----------------------------------------------------------
//  byte[] publicKeyMult( byte[] in_pubkey, byte[] in_tweak );
// -----------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_publicKeyMult
  ( JNIEnv * env, jobject obj, jbyteArray in_pubkey, jbyteArray in_tweak )
{
  // java to C
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  jsize len = (*env)->GetArrayLength( env, in_pubkey );
  if ( NULL == jkey ) return NULL;
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // deserialize public key

  secp256k1_pubkey pubkey;
  if ( 1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, (size_t)len) )
    return NULL;

  jbyte* jtweak = (*env)->GetByteArrayElements( env, in_tweak, NULL );
  len = (*env)->GetArrayLength( env, in_tweak );
  if (NULL == jtweak || (jsize)TWKSIZE != len) return NULL;
  unsigned char * tweakbytes = (unsigned char *)jtweak;

  // operation

  if (1 != secp256k1_ec_pubkey_tweak_mul(pCONTEXT, &pubkey, tweakbytes) ) return NULL;

  // convert result from opaque to serial pubkey

  unsigned char serialPubKey[ PUBKEYSZ ];
  size_t outLen = (size_t)PUBKEYSZ;

  (void)secp256k1_ec_pubkey_serialize( pCONTEXT,
                                       (unsigned char *)serialPubKey,
                                       &outLen,
                                       &pubkey,
                                       compressed );

  jbyteArray result = (*env)->NewByteArray( env, PUBKEYSZ );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, PUBKEYSZ, serialPubKey );

  return result;
}

// ------------------------------------------------------
//  byte[] signECDSA( byte[] hash32, byte[] in_seckey );
// ------------------------------------------------------

JNIEXPORT jbyteArray JNICALL Java_a_keymaster_cryptils_Secp256k1_signECDSA
  (JNIEnv * env, jobject obj, jbyteArray hash32, jbyteArray in_seckey )
{
  // java to C - hash32

  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  jsize len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return NULL;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // java to C - key

  jbyte* jkey = (*env)->GetByteArrayElements( env, in_seckey, NULL );
  len = (*env)->GetArrayLength( env, in_seckey );
  if ( NULL == jkey || (jsize)PVTKEYSZ != len ) return NULL;
  unsigned char * seckeybytes = (unsigned char *)jkey;

  // compute opaque signature

  secp256k1_ecdsa_signature sig;

  if ( 1 != secp256k1_ecdsa_sign(pCONTEXT, &sig, hashbytes, seckeybytes, NULL, NULL) )
    return NULL;

  // serialize signature into DER format

  unsigned char output[74]; // 2 x 32bytes + 10 bytes overhead
  size_t outlen = (size_t)74;

  if ( 1 != secp256k1_ecdsa_signature_serialize_der(pCONTEXT, output, &outlen, &sig) )
    return NULL;

  // C to java - result

  jbyteArray result = (*env)->NewByteArray( env, outlen );
  if (NULL != result)
    (*env)->SetByteArrayRegion( env, result, 0, outlen, output );

  return result;
}

// --------------------------------------------------------------------------
//  boolean verifyECDSA( byte[] signature, byte[] hash32, byte[] in_pubkey );
// --------------------------------------------------------------------------

JNIEXPORT jboolean JNICALL Java_a_keymaster_cryptils_Secp256k1_verifyECDSA
( JNIEnv * env,
  jobject obj,
  jbyteArray signature,
  jbyteArray hash32,
  jbyteArray in_pubkey )
{
  // java to C - serialized signature
  jbyte* jsig = (*env)->GetByteArrayElements( env, signature, NULL );
  if ( NULL == jsig ) return JNI_FALSE;
  jsize len = (*env)->GetArrayLength( env, signature );
  unsigned char * sigbytes = (unsigned char *)jsig;

  // convert signature from serialized to opaque form
  secp256k1_ecdsa_signature sig;

  if ( 1 != secp256k1_ecdsa_signature_parse_der(pCONTEXT, &sig, sigbytes, len) )
    return JNI_FALSE;

  // java to C - pubkey
  jbyte* jkey = (*env)->GetByteArrayElements( env, in_pubkey, NULL );
  if ( NULL == jkey ) return JNI_FALSE;
  len = (*env)->GetArrayLength( env, in_pubkey );
  unsigned char * pubkeybytes = (unsigned char *)jkey;

  // convert pubkey from serialized to opaque form

  secp256k1_pubkey pubkey;
  if (1 != secp256k1_ec_pubkey_parse(pCONTEXT, &pubkey, pubkeybytes, len))
    return JNI_FALSE;

  // java to C - hash32
  jbyte* jh32 = (*env)->GetByteArrayElements( env, hash32, NULL );
  len = (*env)->GetArrayLength( env, hash32 );
  if ( NULL == jh32 || 32 != len ) return JNI_FALSE;
  unsigned char * hashbytes = (unsigned char *)jh32;

  // verify signature

  int iresult = secp256k1_ecdsa_verify( pCONTEXT, &sig, hashbytes, &pubkey );

  if (1 == iresult) return JNI_TRUE;
  return JNI_FALSE;
}

