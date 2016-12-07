package in.trujobs.dev.trudroid.interview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.SearchJobsActivity;
import in.trujobs.dev.trudroid.TruJobsBaseActivity;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.prescreen.InterviewSlotSelectFragment;

public class InterviewSlotSelectActivity extends TruJobsBaseActivity {

    private static Long jobPostId;
    private static String preScreenCompanyName;
    private static String preScreenJobRoleTitle;
    private static String preScreenJobTitle;
    private boolean doubleBackToExitPressedOnce = false;

    public static void start(Context context, Long jpId, String companyName,
                             String jobRoleTitle, String jobTitle) {
        Intent intent = new Intent(context, InterviewSlotSelectActivity.class);
        Tlog.i("Starting interview activity for jobPostId");
        jobPostId = jpId;
        preScreenCompanyName = companyName;
        preScreenJobRoleTitle = jobRoleTitle;
        preScreenJobTitle = jobTitle;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview_slot_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InterviewSlotSelectFragment interviewSlotSelectFragment = new InterviewSlotSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putString("companyName", preScreenCompanyName);
        bundle.putString("jobRoleTitle", preScreenJobRoleTitle);
        bundle.putString("jobTitle", preScreenJobTitle);
        bundle.putLong("jobPostId", jobPostId);
        interviewSlotSelectFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(R.id.activity_interview_slot_select, interviewSlotSelectFragment).commit();

    }


    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //Track this action
            addActionGA(Constants.GA_SCREEN_NAME_SELECT_INTERVIEW_SLOT, Constants.GA_ACTION_TRIED_INTERVIEW_EXIT);

            Intent intent = new Intent(InterviewSlotSelectActivity.this, SearchJobsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            this.finish();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        showToast("Press back again to cancel Interview Scheduling.");

        //Track this action
        addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_TRIED_EXIT);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2500);
    }
}
