package in.trujobs.dev.trudroid.Util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobRoleResponse;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Util {
    public static boolean isLoggedIn() {
        boolean loginStatus = false;

        if(Prefs.leadId.get() != 0L){
            loginStatus = true;
        }
        return loginStatus;
    }

    public static int isValidName(String name) {
        if (name.trim().length() == 0){
            return 0;
        } else if(!name.matches("^[ A-z]+$")){
            return 1;
        } else {
            return 2;
        }
    }

    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String pass) {
        if (pass != null && pass.length() > 3) {
            return true;
        }
        return false;
    }

    public static boolean isValidMobile(String mobile) {
        if (mobile.length() != 10)
            return false;
        char first_digit = mobile.charAt(0);
        if ((first_digit < '7') || (first_digit > '9')) {
            return false;
        } else {
            return true;
        }
    }

    public static JobRoleResponse getJobRoleResponse(Activity context) {
        JobRoleResponse jobRoleResponse = null;
        jobRoleResponse = HttpRequest.getJobRoles();
        return jobRoleResponse;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}