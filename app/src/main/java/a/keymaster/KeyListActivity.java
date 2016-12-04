package a.keymaster;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import a.keymaster.cryptils.HexString;
import a.keymaster.cryptils.SHA256;
import a.keymaster.cryptils.Secp256k1;

public class KeyListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<KeyRow> keys_;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_key_list );

        keys_ = getKeys();

        ListView keylist = (ListView) findViewById( R.id.key_list );
        keylist.setOnItemClickListener( this );
        KeyListAdapter adapter = new KeyListAdapter( this, keys_ );
        keylist.setAdapter( adapter );
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        keys_ = getKeys();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private ArrayList<KeyRow> getKeys() {
        ArrayList<KeyRow> results = new ArrayList<>();

        String[] knames =
                new KeyRing( getSharedPreferences(Globals.instance().getPrefsName(), MODE_PRIVATE) )
                .names();

        if ( null == knames || 0 == knames.length ) return results;

        for ( String name : knames ) results.add( new KeyRow(name) );

        return results;
    }

    // user has clicked a button in the key list
    public void doSomething( View v ) {
        Button b = (Button) v;
        String cmd = b.getText().toString().trim();

        Intent intent = null;

        if (cmd.equalsIgnoreCase(getString(R.string.kla_use))) {
            try {
                intent = new Intent( "com.google.zxing.client.android.SCAN" );
                intent.putExtra( "SCAN_MODE", "QR_CODE_MODE" );
                intent.putExtra( "SAVE_HISTORY", false );
                startActivityForResult( intent, 0 );
                return; // dont fall through and restart the intent or it just wont return
            } catch (Exception e) {
                Uri marketUri = Uri.parse( "market://details?id=com.google.zxing.client.android" );
                intent = new Intent( Intent.ACTION_VIEW, marketUri );
            }
        }
        else if (cmd.equalsIgnoreCase(getString(R.string.kla_details))) {
            intent = new Intent( this, KeyDetailsActivity.class );
        }
        else {
            intent = new Intent( this, CreateKeyActivity.class );
        }

        startActivity( intent );
    } // end keyAction

    @Override
    public void onItemClick(AdapterView<?> av, View v, int position, long id ) {
        KeyRow kr = (KeyRow)av.getItemAtPosition( position );
        String kname = (null != kr) ? kr.getKeyName() : "";
        Globals.instance().setSelectedKeyName( kname );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String challenge = data.getStringExtra( "SCAN_RESULT" );

                try {
                    Intent intent = new Intent( this, SignActivity.class );
                      intent.putExtra( "challenge", challenge );
                      startActivity( intent );
                } catch( Exception e ) {
                    Toast.makeText( getApplicationContext(), "oops: " + e.getMessage(), Toast.LENGTH_LONG )
                            .show();
                }
            } else
            if (resultCode == RESULT_CANCELED) {
                ; // dunno
            }
        }
    }
}
