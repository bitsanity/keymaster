package a.keymaster.cryptils;

import java.util.Arrays;

//
// jni wrapper around rmd160.c
//
public class RIPEMD160 {

  static { System.loadLibrary( "keymaster" ); }

  public RIPEMD160() {}

  public native byte[] digest( byte[] message );

  public static void tests( String[] args )
  {
    // test vectors from
    //   http://homes.esat.kuleuven.be/~bosselae/ripemd160.html

    String[] msgs = new String[] {
      "",
      "a",
      "abc",
      "message digest",
      "abcdefghijklmnopqrstuvwxyz",
      "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq",
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789",
        "1234567890123456789012345678901234567890"
      + "1234567890123456789012345678901234567890" };

   String[] dgsts = new String[] {
     "9c1185a5c5e9fc54612808977ee8f548b2258d31",
     "0bdc9d2d256b3ee9daae347be6f4dc835a467ffe",
     "8eb208f7e05d987a9b044a8e98c6b087f15a0bfc",
     "5d0689ef49d2fae572b881b123a85ffa21595f36",
     "f71c27109c692c1b56bbdceb5b9d2865b3708dbc",
     "12a053384a9c0c88e405a06c27dcf49ada62eb2b",
     "b0e20b6e3116640286ed3a87a5713079b21f5189",
     "9b752e45573d4b39f4dbd3323cab82bf63326bfb" };

    byte[] result = null;
    boolean allzwell = true;

    for (int ii = 0; ii < msgs.length; ii++)
    {
      result = new RIPEMD160().digest( msgs[ii].getBytes() );
      allzwell &= Arrays.equals( result, HexString.decode(dgsts[ii]) );
    }

    if (allzwell) System.out.println( "RMD160.main(): PASS" );
      else System.out.println( "RMD160.main(): FAIL" );
  }
}
