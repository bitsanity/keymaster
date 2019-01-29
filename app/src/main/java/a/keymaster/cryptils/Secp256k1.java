package a.keymaster.cryptils;

public class Secp256k1 {
    // expect libkeymaster.so includes libsecp256k1.so
    // all must be x-compiled for arm architecture
    static { System.loadLibrary( "keymaster" ); }

    public Secp256k1() {
        int res = this.resetContext();
    }

    // inits data structure of precomputed tables for speed.
    // only needs to be done once but multiple times does not hurt
    private native int resetContext();

    public native boolean privateKeyIsValid( byte[] in_seckey );

    public native byte[] publicKeyCreate( byte[] in_seckey );

    public native byte[] uncompressPublicKey( byte[] in_pubkey );

    public native byte[] signECDSA( byte[] hash32, byte[] in_seckey );

    public native boolean
    verifyECDSA( byte[] signature, byte[] hash32, byte[] in_pubkey );
}
