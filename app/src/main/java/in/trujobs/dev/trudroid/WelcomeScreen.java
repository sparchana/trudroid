package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.device.yearclass.YearClass;

import in.trujobs.dev.trudroid.CustomDialog.ViewDialog;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.MessageConstants;

public class WelcomeScreen extends TruJobsBaseActivity {

    boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        // track screen view
        addScreenViewGA(Constants.GA_SCREEN_NAME_WELCOME);

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

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_WELCOME, Constants.GA_ACTION_SKIP_TO_SEARCH);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeScreen.this, Login.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_WELCOME, Constants.GA_ACTION_LOGIN_FROM_WELCOME);
            }
        });

        buttonJoinNow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeScreen.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_WELCOME, Constants.GA_ACTION_SIGNUP_FROM_WELCOME);
            }
        });

        TextView termsAndConditions = (TextView) findViewById(R.id.tnc);
        termsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDialog alert = new ViewDialog();
                alert.showDialog(WelcomeScreen.this, "Terms and Conditions",
                        MessageConstants.TERMS_AND_CONDITIONS, "", R.drawable.company_icon, -1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Prefs.jobToApplyStatus.put(0);
            Prefs.getJobToApplyJobId.put(0L);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        showToast("Please press back again to exit");

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2500);
    }
}
