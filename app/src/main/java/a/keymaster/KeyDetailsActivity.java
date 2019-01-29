package a.keymaster;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import a.keymaster.cryptils.*;

public class KeyDetailsActivity extends AppCompatActivity implements Runnable {

    ImageView qr_ = null;
    TextView keyVal_ = null;
    byte[] privKey_ = null;
    String keyBIP38_ = null;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_details);

        qr_ = (ImageView) findViewById( R.id.keydetails_qr );
        keyVal_ = (TextView) findViewById( R.id.keydetails_keyval );

        TextView kname = (TextView) findViewById( R.id.keydetails_keyname );
        kname.setText( Globals.instance().selectedKeyName() );

        KeyRing kr = new KeyRing(getSharedPreferences(Globals.instance().getPrefsName(), MODE_PRIVATE));
        String blkey = kr.pvtKeyBlack( Globals.instance().selectedKeyName() );
        try {
            privKey_ = BDE.decrypt( blkey, Globals.instance().selectedKeyName(), Globals.instance().getPIN() );
        } catch( Exception e ) {
            Toast.makeText( getApplicationContext(), "oops: " + e.getMessage(), Toast.LENGTH_SHORT )
                    .show();
        }

        publicQR_CB( null );
        new Thread(this).start();
    }

    public void publicQR_CB( View v ) {
        if (null == privKey_) {
            Toast.makeText( getApplicationContext(), R.string.kda_waitmsg, Toast.LENGTH_SHORT ).show();
            return;
        }

        try {
            String pubkey = HexString.encode( Globals.instance().curve().publicKeyCreate(privKey_) );
            Bitmap qr = QR.encode( pubkey, Globals.QR_SIZE );
            qr_.setImageBitmap( qr );
            keyVal_.setText( pubkey );

            ClipboardManager clipboard =
              (ClipboardManager)getSystemService( Context.CLIPBOARD_SERVICE );

            ClipData clip = ClipData.newPlainText("simple text", pubkey );
            clipboard.setPrimaryClip( clip );

        } catch( Exception e ) {
            Toast.makeText( getApplicationContext(), "oops: " + e.getMessage(), Toast.LENGTH_SHORT )
                    .show();
        }
    }

    public void privateQR_CB( View v ) {
        if (null == keyBIP38_) {
            Toast.makeText( getApplicationContext(), R.string.kda_waitmsg, Toast.LENGTH_SHORT ).show();
            return;
        }

        try {
            Bitmap qr = QR.encode( keyBIP38_, Globals.QR_SIZE );
            qr_.setImageBitmap( qr );
            keyVal_.setText( keyBIP38_ );

            ClipboardManager clipboard =
              (ClipboardManager)getSystemService( Context.CLIPBOARD_SERVICE );

            ClipData clip = ClipData.newPlainText("simple text", keyBIP38_ );
            clipboard.setPrimaryClip( clip );

        } catch( Exception e ) {
            Toast.makeText( getApplicationContext(), "oops: " + e.getMessage(), Toast.LENGTH_SHORT )
                 .show();
        }
    }

    public void run() {
        try {
            keyBIP38_ = BIP38.encrypt(privKey_, Globals.instance().getPIN());
        }
        catch( Exception e ) {
            Toast.makeText( getApplicationContext(), "oops: " + e.getMessage(), Toast.LENGTH_SHORT )
                 .show();
        }
    }
}
