package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.facebook.device.yearclass.YearClass;

import in.trujobs.dev.trudroid.Util.Tlog;

public class WelcomeScreen extends TruJobsBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        /**
         * Implementation for https://github.com/facebook/device-year-class
         * Device Year Class is an Android library that implements a simple algorithm that maps
         * a device's RAM, CPU cores, and clock speed to the year where those combination of specs were considered high end.
         */
        Tlog.v("DEVICE-YEAR: " + YearClass.get(getApplicationContext()));

        getSupportActionBar().hide();
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.linear_layout_join_now);

        Button buttonLogin = (Button) findViewById(R.id.login_now_btn);
        Button buttonJoinNow = (Button) findViewById(R.id.join_now_btn);
        Button buttonSkip = (Button) findViewById(R.id.skip_btn);

        relativeLayout.setBackgroundResource(R.drawable.join_now_background);
        AnimationDrawable backgroundAnimation = (AnimationDrawable) relativeLayout.getBackground();
        backgroundAnimation.start();

        buttonSkip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeScreen.this, SearchJobsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeScreen.this, Login.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        buttonJoinNow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeScreen.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });
    }
}
