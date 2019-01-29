package a.keymaster.cryptils;

// en.bitcoin.it/wiki/Technical_background_of_version_1_Bitcoin_addresses
public class BitcoinAddress
{
  public BitcoinAddress( byte[] pvkey ) throws Exception
  {
    Secp256k1 curve = new Secp256k1();

    if ( !curve.privateKeyIsValid(pvkey) )
      throw new Exception( "BitcoinAddress(): bad key" );

    byte[] pubkey = curve.uncompressPublicKey( curve.publicKeyCreate(pvkey) );

    if (pubkey.length != 65)
      throw new Exception( "BitcoinAddress(): public key invalid" );

    byte[] hashed = SHA256.hash( pubkey );
    byte[] riped = RIPEMD160.digest( hashed );

    // prepend the 0x00 for MAIN bitcoin network
    byte[] riped2 = ByteOps.prepend( MAIN, riped );

    addressB58Check_ = Base58Check.encode( riped2 );
  }

  public String toString()
  {
    return addressB58Check_;
  }

  private String addressB58Check_ = null;

  private static final byte MAIN = (byte)0x00; // Main network
}
