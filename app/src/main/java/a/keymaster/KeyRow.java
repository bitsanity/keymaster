package a.keymaster;

public class KeyRow {
    public KeyRow() {
        ; // nothing
    }

    public KeyRow( String keyName ) {
        keyName_ = keyName;
    }

    // Property: keyName
    private String keyName_;
    public String getKeyName() { return keyName_; }
    public void setKeyName( String keyName ) { keyName_ = keyName; }

}
