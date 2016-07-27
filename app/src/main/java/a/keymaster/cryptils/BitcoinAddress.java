package a.keymaster.cryptils;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import a.keymaster.Globals;

public class BitcoinAddress {
    public BitcoinAddress( byte[] pvkey ) throws Exception
    {
        byte[] pubkey_ = Globals.instance().curve().publicKeyCreate( pvkey );
        byte[] hashed = SHA256.hash(pubkey_);

        RIPEMD160Digest dig = new RIPEMD160Digest();
        dig.update( hashed, 0, hashed.length );
        byte[] riped = new byte[dig.getDigestSize()];
        dig.doFinal( riped, 0 );

        // prepend the 0x00 for MAIN bitcoin network
        byte[] riped2 = ByteOps.prepend( MAIN, riped );

        addressB58Check_ = Base58Check.encode( riped2 );
    }

    public String publicAddress()
    {
        return addressB58Check_;
    }

    private String addressB58Check_ = null;

    private static final byte MAIN = (byte)0x00; // Main network
}
