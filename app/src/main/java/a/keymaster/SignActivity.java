package a.keymaster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import a.keymaster.cryptils.BDE;
import a.keymaster.cryptils.HexString;
import a.keymaster.cryptils.MessageHolder;
import a.keymaster.cryptils.QR;
import a.keymaster.cryptils.SHA256;

public class SignActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sign );

        TextView challKey = (TextView) findViewById( R.id.sa_challengerkey );
        ImageView qrView = (ImageView) findViewById( R.id.sa_qr);
        //TextView rspTxt = (TextView) findViewById( R.id.sa_responseTxt );

        Intent caller = getIntent();
        String challenge = caller.getStringExtra( "challenge" );

        try {
            MessageHolder hldr = MessageHolder.parse(challenge);
            challKey.setText( HexString.encode(hldr.msg()) );
            KeyRing kr = new KeyRing( getSharedPreferences(Globals.instance().getPrefsName(), MODE_PRIVATE) );
            String keyBl = kr.pvtKeyBlack( Globals.instance().selectedKeyName() );
            byte[] privkey = BDE.decrypt( keyBl, Globals.instance().selectedKeyName(), Globals.instance().getPIN() );
            byte[] mypubkey = Globals.instance().curve().publicKeyCreate( privkey );

            byte[] sigOfChallenge = Globals.instance().curve().signECDSA( SHA256.hash(hldr.msg()), privkey );

            MessageHolder mh = new MessageHolder( mypubkey, sigOfChallenge );
            String response = mh.toString();
            Bitmap qr = QR.encode( response, Globals.QR_WIDTH, Globals.QR_HEIGHT );
            qrView.setImageBitmap( qr );
            //rspTxt.setText( response );

        } catch (Exception e) {
            Toast.makeText( getApplicationContext(), "oops: " + e.getMessage(),
                    Toast.LENGTH_LONG ).show();
        }
    }
}
