package a.keymaster;

import android.content.SharedPreferences;

public class KeyRing {
    private SharedPreferences prefs_ = null;

    public KeyRing( SharedPreferences prefs ) {
        prefs_ = prefs;
    }

    public boolean nameExists( String name ) {
        String keyList = null;

        try {
            keyList = prefs_.getString( Globals.instance().getKeyPrefName(), null );
            String[] allPairs = (null != keyList) ? keyList.split("#") : null;

            if (null != allPairs)
                for (String pair : allPairs) {
                    String[] parts = pair.split(":");
                    if ( parts[0].contains(name) )
                        return true;
                }
        } catch (Exception e) { }

        return false;
    }

    public void appendKey( String name, String pkeyBlack ) {
        String keyList = prefs_.getString( Globals.instance().getKeyPrefName(), "" );

        if (null == name || 0 == name.length()
                || null == pkeyBlack || 0 == pkeyBlack.length() )
            return;

        if (0 < keyList.length()) keyList += "#";

        keyList += name + ":" + pkeyBlack;

        SharedPreferences.Editor ed = prefs_.edit();
        ed.putString( Globals.instance().getKeyPrefName(), keyList );
        ed.apply();
    }

    private String getKeyPart( String kname, int part ) {
        String keyList = prefs_.getString( Globals.instance().getKeyPrefName(), "" );

        if ( 0 == keyList.length() || null == kname || 0 == kname.length() ) return null;

        String[] allPairs = keyList.split( "#" );

        for ( String pair : allPairs ) {
            String[] parts = pair.split(":");
            if (parts[0].contains(kname))
                return parts[part];
        }

        return null;
    }

    public String pvtKeyBlack( String name ) {return getKeyPart( name, 1 ); }

    public String[] names() {
        String[] result = new String[0];

        String keyList = prefs_.getString( Globals.instance().getKeyPrefName(), null );
        if (null == keyList || 0 == keyList.length()) return result;

        String[] allPairs = keyList.split("#");
        if (0 == allPairs.length) return result;

        result = new String[ allPairs.length ];

        for (int ii = 0; ii < allPairs.length; ii++ )
            result[ii] = allPairs[ii].split(":")[0];

        return result;
    }
}
