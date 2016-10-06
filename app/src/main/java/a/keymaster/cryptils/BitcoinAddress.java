package a.keymaster.cryptils;

import a.keymaster.Globals;

public class BitcoinAddress {
  public BitcoinAddress( byte[] pvkey ) throws Exception
  {
    byte[] pubkey_ = Globals.instance().curve().publicKeyCreate( pvkey );
    byte[] hashed = SHA256.hash(pubkey_);

    byte[] riped = new RIPEMD160().digest( hashed );

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
