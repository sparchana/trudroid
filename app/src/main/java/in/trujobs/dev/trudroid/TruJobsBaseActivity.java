package in.trujobs.dev.trudroid;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.CheckNetworkStatus;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by zero on 20/8/16.
 */
public class TruJobsBaseActivity extends AppCompatActivity implements CheckNetworkStatus {
    private Toast mBaseToastLong;
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
}
