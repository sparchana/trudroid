package in.trujobs.dev.trudroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.ServerConstants;

public class SplashScreenActivity extends AppCompatActivity {

    private final int SPLASH_TIME_OUT = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();

        if (!Util.isConnectedToInternet(this)) {
/*            ViewDialog alert = new ViewDialog();
            alert.showDialog(SplashScreenActivity.this, "No Internet", R.drawable.job_apply);*/
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
                    finish();
                    Intent intent = new Intent(SplashScreenActivity.this, JobActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                } else{
                    finish();
                    Intent intent = new Intent(SplashScreenActivity.this, JoinNow.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                }
            }
        };
    }
}
