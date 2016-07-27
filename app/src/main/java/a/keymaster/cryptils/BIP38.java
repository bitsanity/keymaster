package a.keymaster.cryptils;

import com.lambdaworks.crypto.SCrypt;

import java.util.Arrays;

// warning: performance of SCrypt is very poor - multiplle different implementations have
//          same problem. Use very sparingly like for export only - can take 45 seconds
//          to over 1 minute on Samsung Galaxy Nexus

public class BIP38 {
    public static String encrypt( byte[] pvkey, String pphrase ) throws Exception
    {
        String addr = new BitcoinAddress( pvkey ).publicAddress();

        byte[] addresshash =
                Arrays.copyOfRange( SHA256.doubleHash(addr.getBytes("ASCII")), 0, 4 );

        byte[] derKey = SCrypt.scrypt(pphrase.getBytes("UTF-8"),
                addresshash,
                SCRYPT_N,
                SCRYPT_R,
                SCRYPT_P,
                SCRYPT_LEN);

        byte[] derivedHalf1 = Arrays.copyOfRange( derKey, 0, 32 );
        byte[] derivedHalf2 = Arrays.copyOfRange( derKey, 32, 64 );

        byte[] block1 = new byte[16];
        byte[] block2 = new byte[16];

        for ( int ii = 0; ii < 16; ii++ )
        {
            block1[ii] = (byte)( pvkey[ii] ^ derivedHalf1[ii] );
            block2[ii] = (byte)( pvkey[ii+16] ^ derivedHalf1[ii+16] );
        }

        AES256 aes = new AES256( derivedHalf2 );

        byte[] encryptedHalf1 = aes.encrypt( block1 );
        byte[] encryptedHalf2 = aes.encrypt( block2 );

        byte[] header = { (byte)0x01, (byte)0x42, FLAGBYTE };

        byte[] epk = ByteOps.concat( header, addresshash );
        epk = ByteOps.concat( epk, encryptedHalf1 );
        epk = ByteOps.concat( epk, encryptedHalf2 );

        return Base58Check.encode( epk );
    }

    public static byte[] decrypt( String enckey, String pphrase ) throws Exception
    {
        byte[] rawkey = Base58Check.decode( enckey );

        if (null == rawkey) throw new Exception( "BIP38.decrypt: null enckey" );
        if (39 != rawkey.length)
            throw new Exception( "BIP38.decrypt: length must be 39, got " + rawkey.length );
        if ( (byte)0x01 != rawkey[0] )
            throw new Exception( "BIP38.decrypt: first byte myst be 0x01" );
        if ( (byte)0x42 != rawkey[1] )
            throw new Exception( "BIP38.decrypt: second byte myst be 0x42" );

        byte[] addresshash = Arrays.copyOfRange( rawkey, 3, 7 );

        byte[] derKey = SCrypt.scrypt( pphrase.getBytes("UTF-8"),
                addresshash,
                SCRYPT_N,
                SCRYPT_R,
                SCRYPT_P,
                SCRYPT_LEN );

        byte[] derHalf1 = Arrays.copyOfRange( derKey, 0, 32 );

        byte[] pvkeyenc = Arrays.copyOfRange(rawkey, 7, 39);
        byte[] pvkeyxored = new AES256( derHalf1 ).decrypt( pvkeyenc );
        return ByteOps.xor( pvkeyxored, derHalf1 );
    }

    // FLAGBYTE bitmap:
    // 1100 0000  (0xC0) - set for non-EC-multiplied keys
    // 0010 0000  (0x20) - set if convert with compressed pub key fmt
    // 0001 0000  (0x10) - reserved for future spec, must be zero
    // 0000 1000  (0x08) - reserved for future spec, must be zero
    // 0000 0100  (0x04) - must be 0 for non-EC-multiplied keys

    private static final byte FLAGBYTE = (byte)0b1100_0000;

    private static final int SCRYPT_N = 16384;
    private static final int SCRYPT_R = 8;
    private static final int SCRYPT_P = 8;
    private static final int SCRYPT_LEN = 64;
}
