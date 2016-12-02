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

import in.trujobs.dev.trudroid.Helper.ApplyJobResponseBundle;
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
    public static Stack propertyIdStack = new Stack();
    public static Stack propertyIdBackStack = new Stack();
    public static Stack otherPropertyIdStack = new Stack();
    public static PreScreenPopulateProtoResponse globalPreScreenPopulateResponse;
    public static Context mContext;
    private android.os.AsyncTask<PreScreenPopulateProtoRequest, Void, PreScreenPopulateProtoResponse> mAsyncTaskPreScreen;

    private static AsyncTask<CheckInterviewSlotRequest, Void, CheckInterviewSlotResponse> checkInterviewSlotAsyncTask;

    protected static Long jobPostId;
    protected static ApplyJobResponseBundle applyJobResponseBundle ;
    public static boolean interviewSlotOpenned = false;
    boolean doubleBackToExitPressedOnce = false;


    public static void start(Context context, Long jpId, ApplyJobResponseBundle responseBundle) {
        Intent intent = new Intent(context, PreScreenActivity.class);
        jobPostId = jpId;
        applyJobResponseBundle = responseBundle;
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
        openPreScreenWizard(jobPostId, applyJobResponseBundle);
    }


    private void openPreScreenWizard(Long jobPostId, ApplyJobResponseBundle applyJobResponseBundle) {
        PreScreenPopulateProtoRequest.Builder requestBuilder = PreScreenPopulateProtoRequest.newBuilder();
        requestBuilder.setJobPostId(jobPostId);
        requestBuilder.setCandidateMobile(String.valueOf(Prefs.candidateMobile.get()));

//        ViewDialog alert = new ViewDialog();
//        ApplyJobResponse applyJobResponse = applyJobResponseBundle.getApplyJobResponse();
//        applyingJobColor = applyJobResponseBundle.getApplyingJobColor();
//        applyingJobButton = applyJobResponseBundle.getApplyingJobButton();
//        applyingJobButtonDetail = applyJobResponseBundle.getApplyingJobButtonDetail();
//        if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_SUCCESS) {
//            alert.showDialog(getApplicationContext(), "Application Sent", "Your Application has been sent to the recruiter", "You can track your application in \"My Jobs\" option in the Menu", R.drawable.sent, 5);
//            //setting "already applied" to apply button of the jobs list
//            try {
//                applyingJobColor.setImageResource(R.drawable.orange_dot);
//                applyingJobButton.setEnabled(false);
//                applyingJobButton.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.back_grey_dark));
//                applyingJobButton.setText("Applied");
//            } catch (Exception ignored){}
//
//            //setting "already applied" to job detail activity button
//            try {
//                applyingJobButtonDetail.setText("Applied");
//                applyingJobButtonDetail.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.back_grey_dark));
//                applyingJobButtonDetail.setEnabled(false);
//            } catch (Exception ignored){}
//        } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_ALREADY_APPLIED) {
//            alert.showDialog(getApplicationContext(), "Already Applied", "Looks like you have already applied to this job", "", R.drawable.sent, 5);
//            try {
//                applyingJobButton.setEnabled(false);
//                applyingJobButton.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.back_grey_dark));
//                applyingJobButton.setText("Applied");
//            } catch (Exception ignored){}
//        } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_JOB) {
//            alert.showDialog(getApplicationContext(), "No Job Found", "Looks like the job is no more active", "", R.drawable.sent, 0);
//        } else if(applyJobResponse.getStatusValue() == ServerConstants.JOB_APPLY_NO_CANDIDATE) {
//            alert.showDialog(getApplicationContext(), "Candidate doesn't exists", "Please login to continue", "", R.drawable.sent, 0);
//        } else if(!Util.isConnectedToInternet(getApplicationContext())) {
//            Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
//        } else {
//            alert.showDialog(getApplicationContext(), "Something went wrong! Please try again", "Unable to contact our servers", "",  R.drawable.sent, 0);
//        }

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
            Tlog.i("should show the prescreen flow : " + preScreenPopulateResponse.getShouldShow());
            if(preScreenPopulateResponse.getShouldShow()){

            }
            Tlog.i(preScreenPopulateResponse.getPropertyIdList().size() + "--> List count");
            // check object for only which property id is available and at the same time object should be initialized and propertyId should match

            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (preScreenPopulateResponse == null) {
                Toast.makeText(getApplicationContext(), "Something went wrong. Please try again later.",
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null Response");
                return;
            }
            globalPreScreenPopulateResponse = preScreenPopulateResponse;
            propertyIdStack = new Stack();
            propertyIdBackStack = new Stack();

            Stack hpQueue = new Stack(); // all solo fragment prop ids, {0,1,4, 5}
            Stack lpQueue = new Stack(); // all in one fragment prop id, {rest}

            Tlog.i("size: " + preScreenPopulateResponse.getPropertyIdList().size());
            if(preScreenPopulateResponse.getPropertyIdCount() > 0) {
                for(Integer propId : preScreenPopulateResponse.getPropertyIdList()){
                    if (propId == 0 || propId == 1 || propId == 4 || propId == 5) {
                        hpQueue.push(propId);
                    } else {
                        lpQueue.push(propId);
                    }
                }

                while (!lpQueue.isEmpty()) {
                    propertyIdStack.push(lpQueue.pop());
                }
                while (!hpQueue.isEmpty()) {
                    propertyIdStack.push(hpQueue.pop());
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
            Tlog.e("Property Id Queue empty");
            PreScreenActivity.triggerInterviewFragment(activity,
                    preScreenPopulateResponse.getPreScreenCompanyName(),
                    preScreenPopulateResponse.getPreScreenJobRoleTitle(),
                    preScreenPopulateResponse.getPreScreenJobTitle(), true);
        } else {
            propId = (Integer) propertyIdStack.pop();
            if(!(propertyIdBackStack.contains(propId))) {
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

//        ////
//        InterviewSlotSelectFragment interviewSlotSelectFragment = new InterviewSlotSelectFragment();
//
//        bundle.putByteArray("asset", preScreenPopulateResponse.getAssetList().toByteArray());
//        bundle.putString("companyName", preScreenPopulateResponse.getPreScreenCompanyName());
//        bundle.putString("jobRoleTitle", preScreenPopulateResponse.getPreScreenJobTitle());
//        bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobRoleTitle());
//        bundle.putLong("jobPostId", preScreenPopulateResponse.getJobPostId());
//
//        interviewSlotSelectFragment.setArguments(bundle);
//        activity.getSupportFragmentManager().beginTransaction()
//                .addToBackStack(null)
//                .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
//                .replace(R.id.pre_screen, interviewSlotSelectFragment).commit();
//
//
//        ////

        switch (propId) {
            case PROPERTY_TYPE_DOCUMENT : // documents
                bundle = new Bundle();
                PreScreenDocument document = new PreScreenDocument();
                bundle.putByteArray("document", preScreenPopulateResponse.getDocumentList().toByteArray());
                bundle.putBoolean("isFinalFragment", propertyIdStack.size() == 0);

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
               bundle.putString("jobRoleTitle", preScreenPopulateResponse.getPreScreenJobTitle());
               bundle.putString("jobTitle", preScreenPopulateResponse.getPreScreenJobRoleTitle());
               bundle.putLong("jobPostId", preScreenPopulateResponse.getJobPostId());
               bundle.putBoolean("isFinalFragment", true);
               others.setArguments(bundle);
               activity.getSupportFragmentManager().beginTransaction()
                       .addToBackStack(null)
                       .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                       .add(R.id.pre_screen, others).commit();
               break;
        }
    }

    public static void triggerInterviewFragment(FragmentActivity activity, String preScreenCompanyName, String preScreenJobRoleTitle, String preScreenJobTitle, boolean isFinalFragment){
        if(isFinalFragment) {
            // check if interview should open
            CheckInterviewSlotRequest.Builder interviewSlotCheckBuilder = CheckInterviewSlotRequest.newBuilder();
            interviewSlotCheckBuilder.setJobPostId(jobPostId);

            checkInterviewSlotAsyncTask = new CheckInterviewSlotAsyncTask(activity, preScreenCompanyName, preScreenJobRoleTitle, preScreenJobTitle);
            checkInterviewSlotAsyncTask.execute(interviewSlotCheckBuilder.build());

        } else {
            // show successfully applied message and redirect to search screen
            Toast.makeText(activity, "Application Successfully completed.",
                    Toast.LENGTH_LONG).show();
            redirectToSearch(mContext);
        }
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
        Tlog.i("back pressed");
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            Intent intent = new Intent(PreScreenActivity.this, SearchJobsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            this.finish();
            return;
        }
        if(interviewSlotOpenned){
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                //Track this action
//                addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_EXIT);

                Intent intent = new Intent(PreScreenActivity.this, SearchJobsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                interviewSlotOpenned = false;
                this.finish();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            showToast("Press back again to  cancel Interview Scheduling.");

            //Track this action
            addActionGA(Constants.GA_SCREEN_NAME_SEARCH_JOBS, Constants.GA_ACTION_TRIED_EXIT);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2500);
            return;

        } else if(propertyIdStack.isEmpty() && !otherPropertyIdStack.isEmpty()){
            // restore all ids of other fragment
            Tlog.i("adding all other prop id to propidstack");
            while(!otherPropertyIdStack.isEmpty()){
                Tlog.i("added : " + otherPropertyIdStack.peek());
                propertyIdStack.push(otherPropertyIdStack.pop());
            }
            interviewSlotOpenned = false;
            otherPropertyIdStack.clear();
        } else if(!propertyIdBackStack.isEmpty() && !propertyIdStack.contains(propertyIdBackStack.peek())){
            Tlog.i("onbackpress case 2");
            propertyIdStack.push(propertyIdBackStack.pop());
            if(!propertyIdBackStack.isEmpty()) Tlog.i("bStack top:" + propertyIdBackStack.peek());
            if(!propertyIdStack.isEmpty()) Tlog.i("mStack top:" + propertyIdStack.peek());
        } else {
            Tlog.i("onbackpress case 3");
            if(!propertyIdBackStack.isEmpty()) Tlog.i("m contains bStack top:" + propertyIdBackStack.peek());
        }
        interviewSlotOpenned =false;
        super.onBackPressed();
    }
}
