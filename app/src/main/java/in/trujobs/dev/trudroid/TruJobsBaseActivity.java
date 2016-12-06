package in.trujobs.dev.trudroid;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import in.trujobs.dev.trudroid.Util.CheckNetworkStatus;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by zero on 20/8/16.
 */
public class TruJobsBaseActivity extends AppCompatActivity implements CheckNetworkStatus {
    private Toast mBaseToastLong;
    private Tracker mTracker;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckNetworkStatus();
        //setting default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/trufont.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        Tlog.i("TruJobsBaseActivity Parent onCreate triggered");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean CheckNetworkStatus() {
        if (!Util.isConnectedToInternet(this)) {
            showToast("Please turn on your wifi/mobile data in order to use this feature");
            return false;
        }
        return true;
    }

    public void addScreenViewGA(String screenName) {

        if (BuildConfig.DEBUG) {
            // do nothing
            return;
        }

        Trudroid application = (Trudroid) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void addActionGA(String screenName, String actionName) {

        if (BuildConfig.DEBUG) {
            // do nothing
            return;
        }

        // Obtain the shared Tracker instance.
        Trudroid application = (Trudroid) getApplication();
        mTracker = application.getDefaultTracker();

        // Track this action
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Android_App_Action")
                .setAction(actionName)
                .build());
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String msg) {
        try{ mBaseToastLong.getView().isShown();     // true if visible
            mBaseToastLong.setText(msg);
        } catch (Exception e) {         // invisible if exception
            mBaseToastLong = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
        }
        mBaseToastLong.show();
    }

    protected boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

}
