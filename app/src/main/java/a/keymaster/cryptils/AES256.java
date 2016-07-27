package a.keymaster.cryptils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES256 {
    public AES256( byte[] aeskey ) throws Exception
    {
        sks_ = new SecretKeySpec( aeskey, KEYSPEC );
    }

    public byte[] encrypt( byte[] red16 ) throws Exception
    {
        Cipher cipher = Cipher.getInstance( CIPHER );
        cipher.init( Cipher.ENCRYPT_MODE, sks_ );
        return cipher.doFinal( red16 );
    }

    public byte[] decrypt( byte[] blk16 ) throws Exception
    {
        Cipher cipher = Cipher.getInstance( CIPHER );
        cipher.init( Cipher.DECRYPT_MODE, sks_ );
        return cipher.doFinal( blk16 );
    }

    private SecretKeySpec sks_;
    private static final String CIPHER = "AES/ECB/NoPadding";
    private static final String KEYSPEC = "AES";
}
