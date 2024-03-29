package in.trujobs.dev.trudroid;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import in.trujobs.dev.trudroid.Util.Prefs;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Trudroid extends MultiDexApplication {

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.File.init(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
