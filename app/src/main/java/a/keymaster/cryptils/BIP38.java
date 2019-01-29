package a.keymaster.cryptils;

import com.lambdaworks.crypto.SCrypt;

import java.util.Arrays;

// warning: performance of SCrypt is very poor - multiple separate
// implementations have same problem.
//
// Ref:
//   https://github.com/bitcoin/bips/blob/master/bip-0038.mediawiki

public class BIP38 {
  public static String encrypt( byte[] pvkey, String pphrase ) throws Exception
  {
    String addr = new BitcoinAddress( pvkey ).toString();

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

    String result = Base58Check.encode( epk );
    return result;
  }

  public static byte[] decrypt( String enckey, String pphrase ) throws Exception
  {
    byte[] rawkey = Base58Check.decode( enckey );

    if (null == rawkey) throw new Exception( "null enckey" );
    if (39 != rawkey.length) throw new Exception( "length must be 39" );

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
    byte[] derHalf2 = Arrays.copyOfRange( derKey, 32, 64 );

    byte[] pvkeyenc = Arrays.copyOfRange(rawkey, 7, 39);

    byte[] pvkeyxored = new AES256( derHalf2 ).decrypt( pvkeyenc );
    return ByteOps.xor( pvkeyxored, derHalf1 );
  }

  public static void main( String[] args ) throws Exception
  {
    String[] pphrases = new String[] {
      "TestingOneTwoThree",
      "Satoshi"
    };

    // test vectors from ref in header

    byte[] red0 = HexString.decode(
      "0xCBF4B9F70470856BB4F40F80B87EDB90865997FFEE6DF315AB166D713AF433A5" );

    String black = encrypt( red0, pphrases[0] );
    assert( black.equals(
      "6PRVWUbkzzsbcVac2qwfssoUJAN1Xhrg6bNk8J7Nzm5H7kxEbn2Nh2ZoGg") );

    byte[] decrypted = decrypt( black, pphrases[0] );
    assert( Arrays.equals(red0, decrypted) );

    byte[] red1 = HexString.decode(
      "0x09C2686880095B1A4C249EE3AC4EEA8A014F11E6F986D0B5025AC1F39AFBD9AE" );

    black = encrypt( red1, pphrases[1] );
    assert( black.equals(
      "6PRNFFkZc2NZ6dJqFfhRoFNMR9Lnyj7dYGrzdgXXVMXcxoKTePPX1dWByq") );
    decrypted = decrypt( black, pphrases[1] );
    assert( Arrays.equals(red1, decrypted) );

    System.out.println( "BIP38: PASS" );
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
