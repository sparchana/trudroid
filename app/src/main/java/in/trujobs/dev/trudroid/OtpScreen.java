package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Prefs;

public class OtpScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_screen);

        final EditText mUserOtp = (EditText) findViewById(R.id.user_otp_edit_text);
        Button addPasswordBtn = (Button) findViewById(R.id.add_password_btn);
        ImageView otpScreenBackArrow = (ImageView) findViewById(R.id.otp_screen_back_arrow);

        addPasswordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mUserOtp.getText().length() == 0){
                    Toast.makeText(OtpScreen.this, "Please enter the OTP",
                            Toast.LENGTH_LONG).show();
                } else{
                    if(mUserOtp.getText().toString().equals(Prefs.storedOtp.get().toString())){
                        Intent intent = new Intent(OtpScreen.this, EnterPassword.class);
                        startActivity(intent);
                    } else{
                        Toast.makeText(OtpScreen.this, "Oops! Incorrect OTP",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        otpScreenBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
