package in.trujobs.dev.trudroid;

import android.app.Application;

import in.trujobs.dev.trudroid.Util.Prefs;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Trudroid extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.File.init(getApplicationContext());
    }
}
