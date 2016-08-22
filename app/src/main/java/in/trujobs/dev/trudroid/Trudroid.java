package in.trujobs.dev.trudroid;

import android.support.multidex.MultiDexApplication;

import in.trujobs.dev.trudroid.Util.Prefs;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Trudroid extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.File.init(getApplicationContext());
    }
}
