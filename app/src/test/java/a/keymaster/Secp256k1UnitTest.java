package a.keymaster;

import org.junit.Test;

import java.security.SecureRandom;

import a.keymaster.cryptils.SHA256;
import a.keymaster.cryptils.Secp256k1;

import static org.junit.Assert.*;

public class Secp256k1UnitTest {

    @Test
    public void canMakeKeys() throws Exception {
        byte[] privkey = new byte[32]; // 256 bits
        new SecureRandom().nextBytes( privkey );

        assertTrue( new Secp256k1().privateKeyIsValid(privkey) );
    }

    @Test
    public void canSignVerify() throws Exception {
        byte[] privkey = new byte[32]; // 256 bits
        new SecureRandom().nextBytes( privkey );

        Secp256k1 curve = new Secp256k1();
        byte[] pubkey = curve.publicKeyCreate( privkey );
        byte[] sig = curve.signECDSA( pubkey, privkey );

        assertTrue( curve.verifyECDSA(SHA256.hash(pubkey),sig,pubkey) );
    }
}