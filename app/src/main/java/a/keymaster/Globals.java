package a.keymaster;

import a.keymaster.cryptils.Secp256k1;

public class Globals {
    public final static int QR_WIDTH = 650;
    public final static int QR_HEIGHT = 650;

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
