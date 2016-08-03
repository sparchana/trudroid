package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Prefs;

public class OtpScreen extends AppCompatActivity {

    EditText mUserOtpOne, mUserOtpTwo, mUserOtpThree, mUserOtpFour;

    public OtpScreen() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_screen);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserOtpOne = (EditText) findViewById(R.id.user_otp_first_edit_text);
        mUserOtpTwo = (EditText) findViewById(R.id.user_otp_second_edit_text);
        mUserOtpThree = (EditText) findViewById(R.id.user_otp_third_edit_text);
        mUserOtpFour = (EditText) findViewById(R.id.user_otp_fourth_edit_text);
        Button addPasswordBtn = (Button) findViewById(R.id.add_password_btn);

        mUserOtpOne.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() ==1) {
                    mUserOtpTwo.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        mUserOtpTwo.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() ==1) {
                    mUserOtpThree.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        mUserOtpThree.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() ==1) {
                    mUserOtpFour.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        mUserOtpFour.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() ==1) {
                    checkOtp();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        addPasswordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkOtp();
            }
        });
    }

    public void checkOtp(){
        if(mUserOtpOne.getText().length() == 0 || mUserOtpTwo.getText().length() == 0 || mUserOtpThree.getText().length() == 0 || mUserOtpFour.getText().length() == 0) {
            Toast.makeText(OtpScreen.this, "Please enter the OTP",
                    Toast.LENGTH_LONG).show();
        } else{
            String userOtp;
            userOtp = mUserOtpOne.getText().toString() + mUserOtpTwo.getText().toString() + mUserOtpThree.getText().toString() + mUserOtpFour.getText().toString();
            if(userOtp.equals(Prefs.storedOtp.get().toString())){
                Intent intent = new Intent(OtpScreen.this, EnterPassword.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            } else{
                Toast.makeText(OtpScreen.this, "Oops! Incorrect OTP",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
