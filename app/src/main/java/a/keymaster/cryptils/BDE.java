package a.keymaster.cryptils;

// Brain-Dead Encryption
//
// In a better world we would store private keys in BIP38 format, but ...
// ... performance of BIP38 (when it calls into SCrypt) is unacceptable
// on test device

import java.util.Arrays;

public class BDE {

    public static String encrypt( byte[] pvkey, String kname, String pin )
    throws Exception {
        if (null == pvkey || 32 != pvkey.length)
            throw new Exception( "BDE.encrypt: key invalid" );

        if (!checkArgs(kname,pin))
          throw new Exception( "BDE.encrypt(): bad args" );

        byte[] r1 = Arrays.copyOfRange( pvkey, 0, 16 );
        byte[] r2 = Arrays.copyOfRange( pvkey, 16, 32 );

        AES256 aes = new AES256( SHA256.hash((kname + pin).getBytes()) );
        byte[] b1 = aes.encrypt( r1 );
        byte[] b2 = aes.encrypt( r2 );

        return HexString.encode( ByteOps.concat(b1, b2) );
    }

    public static byte[] decrypt( String black, String kname, String pin )
    throws Exception {
        if (!checkArgs(kname,pin)) throw new Exception( "BDE.encrypt(): bad args" );

        byte[] blackBytes = HexString.decode( black );
        byte[] b1 = Arrays.copyOfRange( blackBytes, 0, 16 );
        byte[] b2 = Arrays.copyOfRange( blackBytes, 16, 32 );

        AES256 aes = new AES256( SHA256.hash((kname + pin).getBytes()) );
        byte[] r1 = aes.decrypt( b1 );
        byte[] r2 = aes.decrypt( b2 );

        return ByteOps.concat( r1, r2 );
    }

    private static boolean checkArgs( String kname, String pin ) {
        return null != kname && 0 < kname.length() && null != pin && 0 < pin.length();
    }
}
