package in.trujobs.dev.trudroid.prescreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.Stack;

import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.SearchJobsActivity;
import in.trujobs.dev.trudroid.TruJobsBaseActivity;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.CheckInterviewSlotRequest;
import in.trujobs.proto.CheckInterviewSlotResponse;
import in.trujobs.proto.PreScreenPopulateProtoRequest;
import in.trujobs.proto.PreScreenPopulateProtoResponse;

import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_DOCUMENT;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_EDUCATION;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_EXPERIENCE;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_LANGUAGE;

public class PreScreenActivity extends TruJobsBaseActivity {
    public static Stack<Integer> propertyIdStack = new Stack<>();
    public static Stack<Integer> propertyIdBackStack = new Stack<>();
    public static Stack<Integer> otherPropertyIdStack = new Stack<>();
    public static PreScreenPopulateProtoResponse globalPreScreenPopulateResponse;
    public static Context mContext;
    private android.os.AsyncTask<PreScreenPopulateProtoRequest, Void, PreScreenPopulateProtoResponse> mAsyncTaskPreScreen;
    public static int totalCountFragment = 0;
    public static int rankCount;

    protected static Long jobPostId;
    public static boolean interviewSlotOpenned = false;
    boolean doubleBackToExitPressedOnce = false;


    public static void start(Context context, Long jpId) {
        Intent intent = new Intent(context, PreScreenActivity.class);
        jobPostId = jpId;
        Tlog.i("Starting prescreen activity for jobpost: "+jpId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this.getApplicationContext();
        if(jobPostId ==null){
            Tlog.e("null jobPostId passed to preScreenActivity");
            return;
        }
        rankCount = 0;
        openPreScreenWizard(jobPostId);
    }


    private void openPreScreenWizard(Long jobPostId) {
        PreScreenPopulateProtoRequest.Builder requestBuilder = PreScreenPopulateProtoRequest.newBuilder();
        requestBuilder.setJobPostId(jobPostId);
        requestBuilder.setCandidateMobile(String.valueOf(Prefs.candidateMobile.get()));

        if (mAsyncTaskPreScreen != null) {
            mAsyncTaskPreScreen.cancel(true);
        }
        mAsyncTaskPreScreen = new PreScreenPopulateAsyncTask(this);
        mAsyncTaskPreScreen.execute(requestBuilder.build());
    }

    public static class CheckInterviewSlotAsyncTask extends AsyncTask<CheckInterviewSlotRequest,
            Void, CheckInterviewSlotResponse> {
        String preScreenCompanyName;
        String preScreenJobRoleTitle;
        String preScreenJobTitle;
        FragmentActivity activity;

        public CheckInterviewSlotAsyncTask(FragmentActivity activity, String preScreenCompanyName, String preScreenJobRoleTitle, String preScreenJobTitle) {
            this.activity = activity;
            this.preScreenCompanyName = preScreenCompanyName;
            this.preScreenJobRoleTitle = preScreenJobRoleTitle;
            this.preScreenJobTitle =  preScreenJobTitle;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected CheckInterviewSlotResponse doInBackground(CheckInterviewSlotRequest... params) {
            return HttpRequest.checkInterviewSlot(params[0]);
        }

        @Override
        protected void onPostExecute(CheckInterviewSlotResponse checkInterviewSlotResponse) {
            super.onPostExecute(checkInterviewSlotResponse);

           if (checkInterviewSlotResponse == null) {
                Tlog.w("Null, checkInterviewResponse");
                return;
            } else if(checkInterviewSlotResponse.getStatus() == CheckInterviewSlotResponse.Status.FAILURE
                   || checkInterviewSlotResponse.getStatus() == CheckInterviewSlotResponse.Status.INVALID){
               Tlog.w("something went wrong, try again");
               Toast.makeText(activity, "Something went wrong, try again",
                       Toast.LENGTH_LONG).show();
           } else if (checkInterviewSlotResponse.getShouldShowInterview()) {
                    showInterviewFragment(activity, preScreenCompanyName, preScreenJobRoleTitle, preScreenJobTitle);
           } else {
               Toast.makeText(activity, "Application Successfully completed.",
                       Toast.LENGTH_LONG).show();
               redirectToSearch(mContext);
           }
        }
    }

    private class PreScreenPopulateAsyncTask extends android.os.AsyncTask<PreScreenPopulateProtoRequest,
            Void, PreScreenPopulateProtoResponse> {
        Context mContext;
        public PreScreenPopulateAsyncTask(Context _mContext) {
            mContext = _mContext;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected PreScreenPopulateProtoResponse doInBackground(PreScreenPopulateProtoRequest... params) {
            return HttpRequest.getJobPostVsCandidate(params[0]);
        }

        @Override
        protected void onPostExecute(PreScreenPopulateProtoResponse preScreenPopulateResponse) {
            super.onPostExecute(preScreenPopulateResponse);

            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (preScreenPopulateResponse == null) {
                Toast.makeText(getApplicationContext(), "Something went wrong. Please try again later.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null Response");
                return;
            }

            // check object for only which property id is available and at the same time object should be initialized and propertyId should match
            globalPreScreenPopulateResponse = preScreenPopulateResponse;
            if(!preScreenPopulateResponse.getShouldShow()){
                propertyIdStack.clear();
                showRequiredFragment(((FragmentActivity) mContext));
                return;
            }

            propertyIdStack = new Stack<>();
            propertyIdBackStack = new Stack<>();

            Stack<Integer> hpStack = new Stack<>(); // all solo fragment prop ids, {0,1,4, 5}
            Stack<Integer> lpStack = new Stack<>(); // all in one fragment prop id, {rest}

            if(preScreenPopulateResponse.getPropertyIdCount() > 0) {
                for(Integer propId : preScreenPopulateResponse.getPropertyIdList()){
                    if(propId == null) continue;
                    if (preScreenPopulateResponse.getHpPropertyIdList().contains(propId)) {
                        hpStack.push(propId);
                    } else {
                        lpStack.push(propId);
                    }
                }
                if(!hpStack.isEmpty()){
                    totalCountFragment = + hpStack.size();
                }
                if(lpStack.size() > 0){
                    totalCountFragment++;
                }
                while (!lpStack.isEmpty()) {
                    propertyIdStack.push(lpStack.pop());
                }
                while (!hpStack.isEmpty()) {
                    propertyIdStack.push(hpStack.pop());
                }
            }
            showRequiredFragment(((FragmentActivity) mContext));
        }
    }
    public static void showRequiredFragment(FragmentActivity activity) {
        PreScreenPopulateProtoResponse preScreenPopulateResponse =  globalPreScreenPopulateResponse;
        Bundle bundle = new Bundle();

        Integer propId = null;
        if(propertyIdStack.isEmpty()){
            Tlog.e("Property Id Stack empty, trigger InterviewFragment");
            PreScreenActivity.triggerInterviewFragment(activity,
                    preScreenPopulateResponse.getPreScreenCompanyName(),
                    preScreenPopulateResponse.getPreScreenJobRoleTitle(),
                    preScreenPopulateResponse.getPreScreenJobTitle());
        } else {
            propId = propertyIdStack.pop();
            if(propId!= null && !(propertyIdBackStack.contains(propId))) {
                propertyIdBackStack.push(propId);
            }
        }

        if(propId == null) {
            return;
        }

        Tlog.i("current Property id: " + propId);
        for(Object item : propertyIdStack){
            Tlog.i(item.toString());
        }
        for(Object item : propertyIdBackStack) {
            Tlog.i("backStack: "+item.toString());
        }

        rankCount++;
        Tlog.i("Rank for :" + rankCount);
        switch (propId) {
            case PROPERTY_TYPE_DOCUMENT : // documents
                bundle = new Bundle();
                PreScreenDocument document = new PreScreenDocument();
                bundle.putByteArray("document", preScreenPopulateResponse.getDocumentList().toByteArray());
                bundle.putBoolean("isFinalFragment", propertyIdStack.size() == 0);
                bundle.putString("companyName", preScreenPopulateResponse.getPreScreenCompanyName());
                bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobTitle());
                bundle.putInt("totalCount",totalCountFragment);
                bundle.putInt("rank",rankCount);

                document.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .add(R.id.pre_screen, document).commit();
                break;
            case PROPERTY_TYPE_LANGUAGE : // language
                PreScreenLanguage language = new PreScreenLanguage();
                bundle.putByteArray("language", preScreenPopulateResponse.getLanguageList().toByteArray());
                bundle.putBoolean("isFinalFragment", propertyIdStack.size() == 0);
                bundle.putString("companyName", preScreenPopulateResponse.getPreScreenCompanyName());
                bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobTitle());
                bundle.putInt("totalCount",totalCountFragment);
                bundle.putInt("rank",rankCount);

                language.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .add(R.id.pre_screen, language).commit();
                break;
            case PROPERTY_TYPE_EXPERIENCE : // exp
                PreScreenExperience experience = new PreScreenExperience();
                bundle.putByteArray("experience", preScreenPopulateResponse.getExperience().toByteArray());
                bundle.putBoolean("isFinalFragment", propertyIdStack.size() == 0);
                bundle.putString("companyName", preScreenPopulateResponse.getPreScreenCompanyName());
                bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobTitle());
                bundle.putInt("totalCount",totalCountFragment);
                bundle.putInt("rank",rankCount);

                experience.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .add(R.id.pre_screen, experience).commit();
                break;
            case PROPERTY_TYPE_EDUCATION : // education
                PreScreenEducation education = new PreScreenEducation();
                bundle.putByteArray("education", preScreenPopulateResponse.getEducation().toByteArray());
                bundle.putLong("jobPostId", preScreenPopulateResponse.getJobPostId());
                bundle.putBoolean("isFinalFragment", propertyIdStack.size() == 0);
                bundle.putString("companyName", preScreenPopulateResponse.getPreScreenCompanyName());
                bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobTitle());
                bundle.putInt("totalCount",totalCountFragment);
                bundle.putInt("rank",rankCount);

                education.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .add(R.id.pre_screen, education).commit();
                break;
           default:
               PreScreenOthers others = new PreScreenOthers();
               // adding the removed propId back
               propertyIdStack.push(propertyIdBackStack.pop());
               bundle.putByteArray("asset", preScreenPopulateResponse.getAssetList().toByteArray());
               bundle.putString("companyName", preScreenPopulateResponse.getPreScreenCompanyName());
               bundle.putString("jobRoleTitle",preScreenPopulateResponse.getPreScreenJobRoleTitle());
               bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobTitle());
               bundle.putLong("jobPostId", preScreenPopulateResponse.getJobPostId());
               bundle.putBoolean("isFinalFragment", true);
               bundle.putInt("totalCount",totalCountFragment);
               bundle.putInt("rank",rankCount);
               others.setArguments(bundle);
               activity.getSupportFragmentManager().beginTransaction()
                       .addToBackStack(null)
                       .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                       .add(R.id.pre_screen, others).commit();
               break;
        }
    }

    public static void triggerInterviewFragment(FragmentActivity activity, String preScreenCompanyName, String preScreenJobRoleTitle, String preScreenJobTitle){
        // check if interview should open
        CheckInterviewSlotRequest.Builder interviewSlotCheckBuilder = CheckInterviewSlotRequest.newBuilder();
        interviewSlotCheckBuilder.setJobPostId(jobPostId);

        AsyncTask<CheckInterviewSlotRequest, Void, CheckInterviewSlotResponse> checkInterviewSlotAsyncTask = new CheckInterviewSlotAsyncTask(activity, preScreenCompanyName, preScreenJobRoleTitle, preScreenJobTitle);
        checkInterviewSlotAsyncTask.execute(interviewSlotCheckBuilder.build());

    }

    public static void showInterviewFragment(FragmentActivity activity, String preScreenCompanyName, String preScreenJobRoleTitle, String preScreenJobTitle){
        InterviewSlotSelectFragment interviewSlotSelectFragment = new InterviewSlotSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putString("companyName", preScreenCompanyName);
        bundle.putString("jobRoleTitle", preScreenJobRoleTitle);
        bundle.putString("jobTitle", preScreenJobTitle);
        bundle.putLong("jobPostId", jobPostId);
        interviewSlotSelectFragment.setArguments(bundle);
        activity.getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                .add(R.id.pre_screen, interviewSlotSelectFragment).commit();
    }

    public static void redirectToSearch(Context mContext){
        Intent intent = new Intent(mContext, SearchJobsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            super.onBackPressed();
            Intent intent = new Intent(PreScreenActivity.this, SearchJobsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            this.finish();
            return;
        } else if(interviewSlotOpenned){
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                interviewSlotOpenned = false;

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_SELECT_INTERVIEW_SLOT, Constants.GA_INTERVIEW_EXIT);

                Intent intent = new Intent(PreScreenActivity.this, SearchJobsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                this.finish();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            showToast("Press back again to cancel interview scheduling.");

            //Track this action
            addActionGA(Constants.GA_SCREEN_NAME_SELECT_INTERVIEW_SLOT, Constants.GA_ACTION_TRIED_INTERVIEW_EXIT);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2500);
            return;

        } else if(propertyIdStack.isEmpty() && !otherPropertyIdStack.isEmpty()){
            // restore all ids of other fragment
            if(rankCount != 0){
                rankCount--;
            }
            while(!otherPropertyIdStack.isEmpty()){
                propertyIdStack.push(otherPropertyIdStack.pop());
            }
            interviewSlotOpenned = false;
            otherPropertyIdStack.clear();
        } else if(!propertyIdBackStack.isEmpty() && !propertyIdStack.contains(propertyIdBackStack.peek())){
            if(rankCount != 0){
                rankCount--;
            }
            propertyIdStack.push(propertyIdBackStack.pop());
        }
        interviewSlotOpenned =false;
        super.onBackPressed();
    }
}
