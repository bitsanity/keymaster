#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "rmd160.h"
#include "a_keymaster_cryptils_RIPEMD160.h"

// ----------------------------------------------
// public native byte[] digest( byte[] message );
// ----------------------------------------------

JNIEXPORT jbyteArray JNICALL
Java_tbox_RIPEMD160_digest ( JNIEnv * env,
                             jclass clss,
                             jbyteArray msg )
{
  jbyte* jmsg = (*env)->GetByteArrayElements( env, msg, NULL );
  jsize len = (*env)->GetArrayLength( env, msg );

  // byte[] to cstring
  char* cbuff = malloc(len + 1);
  memcpy( cbuff, jmsg, len );
  cbuff[len] = '\0';

printf( "msg: %s", cbuff );

  byte* result = RMD( (byte *)cbuff );

  free( cbuff );

printf( "\nres: " );

int ii;
for (ii=0;ii<20;ii++)
  printf( "%02x", result[ii] );


printf( "\n" );
fflush( stdout );

  jbyteArray jresult = (*env)->NewByteArray( env, 20 );

  if (NULL != jresult)
    (*env)->SetByteArrayRegion( env,
                                jresult,  // java target
                                0,        // start pos
                                20,       // num bytes
                                result ); // native source

  return jresult;
}
