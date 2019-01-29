package a.keymaster.cryptils;

import java.math.BigInteger;

public class Base58 {
    public static String encode( byte[] src )
    {
        BigInteger srcInt = new BigInteger( 1, src );

        StringBuffer sbuff = new StringBuffer();

        while ( 0 <= srcInt.compareTo(BASE) )
        {
            BigInteger mod = srcInt.mod( BASE );
            sbuff.insert( 0, LUT.charAt(mod.intValue()) );
            srcInt = srcInt.subtract( mod ).divide( BASE );
        }

        sbuff.insert( 0, LUT.charAt(srcInt.intValue()) );

        for ( int ii = 0; ii < src.length; ii++ )
            if ( 0 == src[ii] )
                sbuff.insert( 0, LUT.charAt(0) );
            else break;

        return sbuff.toString();
    }

    public static byte[] decode( String b58 )
    {
        byte[] raw = toBigInt( b58 ).toByteArray();

        boolean sign = raw.length > 1 && raw[0] == 0 && raw[1] < 0;

        int zeroes = 0;
        for ( int ii = 0; ii < b58.length() && b58.charAt(ii) == LUT.charAt(0); ii++ )
            zeroes++;

        byte[] tmp = new byte[raw.length - (sign ? 1 : 0) + zeroes];

        System.arraycopy( raw, (sign ? 1 : 0), tmp, zeroes, tmp.length - zeroes );

        return tmp;
    }

    private static BigInteger toBigInt( String b58 )
    {
        BigInteger result = BigInteger.valueOf( 0 );

        for ( int ii = b58.length() - 1; 0 <= ii; ii-- )
        {
            int index = LUT.indexOf( b58.charAt(ii) );
            result = result.add(
                    BigInteger.valueOf(index).multiply(BASE.pow(b58.length() - 1 - ii)));
        }

        return result;
    }

    private static final String LUT =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    private static final BigInteger BASE = BigInteger.valueOf(58);
}
