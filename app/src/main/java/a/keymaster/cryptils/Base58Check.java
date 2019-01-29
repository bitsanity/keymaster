package a.keymaster.cryptils;

import java.util.Arrays;

public class Base58Check {
    public static String encode( byte[] extkey ) throws Exception
    {
        byte[] checksum =
                java.util.Arrays.copyOfRange( SHA256.hash(SHA256.hash(extkey)), 0, 4 );

        // append checksum to input
        byte[] toEncode = new byte[ extkey.length + checksum.length ];
        System.arraycopy( extkey, 0, toEncode, 0, extkey.length );
        System.arraycopy( checksum, 0, toEncode, toEncode.length - checksum.length, checksum.length );

        return Base58.encode( toEncode );
    }

    public static byte[] decode( String str ) throws Exception
    {
        byte[] strBytes = Base58.decode( str );

        byte[] data = Arrays.copyOfRange(strBytes, 0, strBytes.length - 4);
        byte[] check = Arrays.copyOfRange( strBytes, strBytes.length - 4, strBytes.length );
        byte[] hashed = Arrays.copyOfRange( SHA256.doubleHash(data), 0, 4 );

        if (!Arrays.equals(hashed, check))
            throw new Exception( "checksum failed, check: " + HexString.encode(check) +
                    ", hashed: " + HexString.encode(hashed) );
        return data;
    }

}
