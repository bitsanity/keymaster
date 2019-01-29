package a.keymaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import a.keymaster.cryptils.*;

public class MainActivity extends AppCompatActivity {
    private StringBuilder pinBldr_ = new StringBuilder();

    private String tmpPIN_;

    private TextView prompt_;
    private Button[] digits_ = new Button[ 10 ];
    private Button clr_;
    private Button del_;
    private RadioButton[] rbs_ = new RadioButton[6];

    private SharedPreferences prefs_;

    private TextView msg_;

    View.OnClickListener listener_ = new View.OnClickListener() {
        public void onClick( View view ) {
            Button src = (Button) view;
            if (src == clr_) {
                pinBldr_ = new StringBuilder();
            } else if (src == del_) {
                if (0 < pinBldr_.length())
                    pinBldr_.deleteCharAt(pinBldr_.length() - 1);
                else
                    pinBldr_ = new StringBuilder();
            } else if (pinBldr_.length() < rbs_.length){
                pinBldr_.append(src.getText());
            }

            // last digit
            if (pinBldr_.length() == rbs_.length) {

                String pin = prefs_.getString( Globals.instance().getPINPrefName(), null );

                if (null == pin) {
                    // first time entered - set tmp and prompt to 'repeat please'
                    if (null == tmpPIN_) {

                        tmpPIN_ = pinBldr_.toString();
                        prompt_.setText( R.string.pin_rptprompt );
                        pinBldr_ = new StringBuilder();

                    } // end if pref not set and first time completed
                    else {
                        if ( tmpPIN_.equals(pinBldr_.toString()) ) {

                            try {
                                SharedPreferences.Editor ed = prefs_.edit();
                                ed.putString( Globals.instance().getPINPrefName(), HexString.encode(SHA256.hash(tmpPIN_.getBytes("UTF-8"))) );
                                ed.apply();
                            } catch (Exception e ) {} // if UTF-8 is not supported we're fscked anyway

                            nextActivity();
                            return;
                        }
                        else {
                            msg_.setText( "WRONG" );
                            pinBldr_ = new StringBuilder();
                        }
                    } // end else temp pin was set and we reentered
                }
                else {
                    // pin exists and we hit the last digit
                    // note we dont store the raw PIN - we store the HASHED pin

                    String candidateS = null;

                    try {
                        candidateS = HexString.encode(SHA256.hash(pinBldr_.toString().getBytes("UTF-8")));
                    } catch (Exception e) {}

                    if (pin.equals(candidateS)) {
                        nextActivity();
                        return;
                    }
                    else {
                        pinBldr_ = new StringBuilder();
                    }
                }
            }

            syncPIN();
        } // end onClick
    }; // end lambda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        digits_[0] = (Button) findViewById(R.id.button0);
        digits_[1] = (Button) findViewById(R.id.button1);
        digits_[2] = (Button) findViewById(R.id.button2);
        digits_[3] = (Button) findViewById(R.id.button3);
        digits_[4] = (Button) findViewById(R.id.button4);
        digits_[5] = (Button) findViewById(R.id.button5);
        digits_[6] = (Button) findViewById(R.id.button6);
        digits_[7] = (Button) findViewById(R.id.button7);
        digits_[8] = (Button) findViewById(R.id.button8);
        digits_[9] = (Button) findViewById(R.id.button9);

        for (int ii = 0; ii < digits_.length; ii++) {
            digits_[ii].setOnClickListener(listener_);
        }

        clr_ = (Button) findViewById(R.id.buttonCLR);
        clr_.setOnClickListener( listener_ );
        del_ = (Button) findViewById(R.id.buttonDEL);
        del_.setOnClickListener( listener_ );

        rbs_[0] = (RadioButton) findViewById( R.id.rb1 );
        rbs_[1] = (RadioButton) findViewById( R.id.rb2 );
        rbs_[2] = (RadioButton) findViewById( R.id.rb3 );
        rbs_[3] = (RadioButton) findViewById( R.id.rb4 );
        rbs_[4] = (RadioButton) findViewById( R.id.rb5 );
        rbs_[5] = (RadioButton) findViewById( R.id.rb6 );
        msg_ = (TextView) findViewById( R.id.space2 );

        prefs_ = getSharedPreferences( Globals.instance().getPrefsName(),
                MODE_PRIVATE);

        prompt_ = (TextView) findViewById( R.id.PINPromptView );
        String pin = prefs_.getString( "HPIN", null );
        if (null == pin)
            prompt_.setText( R.string.pin_newprompt );
        else
            prompt_.setText( R.string.pin_enterprompt );
    }

    private void syncPIN() {
        int len = pinBldr_.length();
        for (int ii = 0; ii < rbs_.length; ii++)
            rbs_[ii].setChecked( len > ii );
    }

    private void nextActivity() {
        Globals.instance().setPIN( pinBldr_.toString() );
        syncPIN();

        Intent intent = new Intent(this, KeyListActivity.class);
        startActivity( intent );
    }
}
