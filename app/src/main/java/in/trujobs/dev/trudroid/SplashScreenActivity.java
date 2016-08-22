package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;

public class SplashScreenActivity extends TruJobsBaseActivity {

    private final int SPLASH_TIME_OUT = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        if (!Util.isConnectedToInternet(this)) {
            Toast.makeText(SplashScreenActivity.this, "No internet connection. Please check your network settings.",
                    Toast.LENGTH_LONG).show();
            new Handler().postDelayed(closeSplashRunnable(), SPLASH_TIME_OUT);
            return;
        }
        new Handler().postDelayed(getSplashRunnable(), SPLASH_TIME_OUT);

    }

    private Runnable closeSplashRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
    }

    private Runnable getSplashRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if(Util.isLoggedIn() == true){
                    Intent intent;
                    if(Prefs.candidateJobPrefStatus.get() == 0){
                        intent = new Intent(SplashScreenActivity.this, JobPreference.class);
                    } else if(Prefs.candidateHomeLocalityStatus.get() == 0){
                        intent = new Intent(SplashScreenActivity.this, HomeLocality.class);
                    } else{
                        intent = new Intent(SplashScreenActivity.this, SearchJobsActivity.class);
/*                        intent = new Intent(SplashScreenActivity.this, CandidateProfileActivity.class);*/

                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    finish();
                } else{
                    Intent intent = new Intent(SplashScreenActivity.this, WelcomeScreen.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    finish();
                }
            }
        };
    }
}
