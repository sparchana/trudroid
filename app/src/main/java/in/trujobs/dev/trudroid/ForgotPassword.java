package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ImageView forgotPasswordBackArrow = (ImageView) findViewById(R.id.forgot_password_back_arrow);
        Button buttonGetMobile = (Button) findViewById(R.id.add_mobile_reset_password_btn);

        buttonGetMobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPassword.this, OtpScreen.class);
                startActivity(intent);
            }
        });

        forgotPasswordBackArrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
