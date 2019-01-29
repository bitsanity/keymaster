package a.keymaster;

import a.keymaster.cryptils.*;

public class Globals {
    public final static int QR_SIZE = 600;

    public static Globals instance() {
        return instance_;
    }

    // property PIN is set upon login and then used to encrypt/decrypt private keys.
    // True PIN is never persisted - only HASH(PIN) is saved
    public String getPIN() {
        return pin_;
    }

    public void setPIN( String pin ) {
        synchronized ( instance_ ) {
            pin_ = pin;
        }
    }

    public Secp256k1 curve() { return curve_; }

    private Secp256k1 curve_;
    private String pin_;

    // in-memory property identifying which key (by name) has been selected for use
    public String selectedKeyName() { return selectedKeyName_; }
    public void setSelectedKeyName( String n ) { selectedKeyName_ = n; }
    private String selectedKeyName_;

    // Names of preference bundle, properties, etc

    public String getPrefsName() {
        return "keymaster";
    }

    public String getPINPrefName() {
        return "HPIN";
    }

    public String getKeyPrefName() {
        return "KEYS";
    }

    private static final Globals instance_ = new Globals();
    private Globals() {
        curve_ = new Secp256k1();
    }
}
