package a.keymaster;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.SecureRandom;
import java.util.Arrays;

import a.keymaster.cryptils.*;

public class CreateKeyActivity extends AppCompatActivity implements Runnable {

    Button[] btns_ = new Button[16];
    byte[] pvkey_;
    TextView rawresult_;
    TextView resPrompt_;
    TextView pubkey_;
    EditText nameField_;

    boolean keepRunning_ = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_key);

        btns_[0] = (Button) findViewById(R.id.btn_0);
        btns_[1] = (Button) findViewById(R.id.btn_1);
        btns_[2] = (Button) findViewById(R.id.btn_2);
        btns_[3] = (Button) findViewById(R.id.btn_3);
        btns_[4] = (Button) findViewById(R.id.btn_4);
        btns_[5] = (Button) findViewById(R.id.btn_5);
        btns_[6] = (Button) findViewById(R.id.btn_6);
        btns_[7] = (Button) findViewById(R.id.btn_7);
        btns_[8] = (Button) findViewById(R.id.btn_8);
        btns_[9] = (Button) findViewById(R.id.btn_9);
        btns_[10] = (Button) findViewById(R.id.btn_10);
        btns_[11] = (Button) findViewById(R.id.btn_11);
        btns_[12] = (Button) findViewById(R.id.btn_12);
        btns_[13] = (Button) findViewById(R.id.btn_13);
        btns_[14] = (Button) findViewById(R.id.btn_14);
        btns_[15] = (Button) findViewById(R.id.btn_15);

        rawresult_ = (TextView) findViewById(R.id.cka_result);
        resPrompt_ = (TextView) findViewById(R.id.resultprompt);
        pubkey_ = (TextView) findViewById(R.id.cka_pubkey);
        nameField_ = (EditText) findViewById(R.id.cka_namefield);
        nameField_.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    KeyRing kr = new KeyRing( getSharedPreferences(Globals.instance().getPrefsName(), MODE_PRIVATE) );

                    if (kr.nameExists( nameField_.getText().toString() )) {
                        nameField_.setText("");
                        Toast nameExists = Toast.makeText(getApplicationContext(), R.string.cka_nameexists, Toast.LENGTH_LONG);
                        nameExists.show();
                        return false;
                    }

                    if (0 >= nameField_.getText().length()) {
                        Toast nameNeeded = Toast.makeText(getApplicationContext(), "Need a name", Toast.LENGTH_LONG);
                        nameNeeded.show();
                        return false;
                    }

                    try {
                        String blkey = BDE.encrypt( pvkey_, nameField_.getText().toString(), Globals.instance().getPIN() );
                        byte[] checkit = BDE.decrypt( blkey, nameField_.getText().toString(), Globals.instance().getPIN() );

                        if (!Arrays.equals(pvkey_, checkit)) {
                          throw new Exception( "Key Enc fail" );
                        }

                        kr.appendKey( nameField_.getText().toString(), blkey );
                    }
                    catch( Exception e) {
                        Toast.makeText( getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG )
                                .show();
                    }

                    Intent intent = new Intent(getApplicationContext(), KeyListActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        rawresult_.setText("");
        resPrompt_.setVisibility(View.INVISIBLE);
        pubkey_.setVisibility(View.INVISIBLE);
        nameField_.setVisibility(View.INVISIBLE);

        for (Button b : btns_)
            b.setEnabled(true);

        // start thread to update button labels with random numbers
        keepRunning_ = true;
        new Thread(this).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop thread that is updating button labels
        keepRunning_ = false;
    }

    public void buttonClicked(View v) {

        Button btn = (Button) v;
        btn.setEnabled(false);

        // append the button label to the raw result
        String newresult = rawresult_.getText().toString() + btn.getText().toString();
        rawresult_.setText(newresult);

        // if last button has been clicked show other fields and provide Pub Key value
        if (64 == newresult.length()) {
            keepRunning_ = false; // stop the worker thread

            resPrompt_.setVisibility(View.VISIBLE);
            pubkey_.setVisibility(View.VISIBLE);
            pubkey_.setText( R.string.cka_wait );

            pubkey_.post(new Runnable() {
                public void run() {
                    try {
                        SecureRandom rnd = new SecureRandom();
                        byte[] rndBytes = new byte[32]; // 256 bits
                        rnd.nextBytes( rndBytes );

                        // combine random with user-specified number for better security
                        byte[] rawBytes = SHA256.hash( rawresult_.getText().toString().getBytes() );
                        byte[] jumbled = ByteOps.xor( rndBytes, rawBytes );
                        pvkey_ = SHA256.hash( jumbled );

                        try {
                            String pub = HexString.encode( Globals.instance().curve().publicKeyCreate(pvkey_) );
                            pubkey_.setText( pub );
                        } catch (Exception e) {
                            pubkey_.setText( e.getMessage() );
                        }
                    } catch (Exception e) {
                        Toast.makeText( getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG )
                                .show();
                    }
                } // end run
            }); // end post

            nameField_.post( new Runnable() {
                public void run() {
                    nameField_.setVisibility( View.VISIBLE );
                }
            } );

        } // end if last button clicked
    } // end buttonclicked

    @Override
    public void run() {
        while (keepRunning_) {
            for (final Button btn : btns_) {
                if (btn.isEnabled()) {

                    SecureRandom x = new SecureRandom();
                    byte[] bytes = new byte[2];
                    x.nextBytes(bytes);
                    final String label = HexString.encode(bytes);

                    // android: cannot update UI from worker thread directly - use View.post
                    btn.post(new Runnable() {
                        public void run() {
                            btn.setText(label);
                        }
                    });
                }

                try {
                    Thread.sleep(125L); // 1/8 seconds, in milliseconds
                } catch (Exception e) {}
            } // end foreach button
        } // end while keep running
    } // end run
} // end CreateKeyActivity

