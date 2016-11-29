package in.trujobs.dev.trudroid.prescreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;

import in.trujobs.dev.trudroid.Helper.ApplyJobResponseBundle;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.TruJobsBaseActivity;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.PreScreenPopulateProtoRequest;
import in.trujobs.proto.PreScreenPopulateProtoResponse;

import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_DOCUMENT;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_EDUCATION;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_EXPERIENCE;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_LANGUAGE;

public class PreScreenActivity extends TruJobsBaseActivity {
    public static Queue propertyIdQueue;
    public static PreScreenPopulateProtoResponse globalPreScreenPopulateResponse;

    private android.os.AsyncTask<PreScreenPopulateProtoRequest, Void, PreScreenPopulateProtoResponse> mAsyncTaskPreScreen;

    private static Long jobPostId;
    private static ApplyJobResponseBundle applyJobResponseBundle ;
    public static void start(Context context, Long jpId, ApplyJobResponseBundle responseBundle) {
        Intent intent = new Intent(context, PreScreenActivity.class);
        Tlog.i("Starting prescreen activity");
        jobPostId = jpId;
        applyJobResponseBundle = responseBundle;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            propertyIdQueue = new LinkedList();

            Queue hpQueue = new LinkedList(); // all solo fragment prop ids, {0,1,4, 5}
            Queue lpQueue = new LinkedList(); // all in one fragment prop id, {rest}

            Tlog.i("size: " + preScreenPopulateResponse.getPropertyIdList().size());
            if(preScreenPopulateResponse.getPropertyIdCount() > 0) {
                for(Integer jobPostId : preScreenPopulateResponse.getPropertyIdList()){
                    if (jobPostId == 0 || jobPostId == 1 || jobPostId == 4 || jobPostId == 5) {
                        hpQueue.add(jobPostId);
                    } else {
                        lpQueue.add(jobPostId);
                    }
                }
                while (!hpQueue.isEmpty()) {
                    propertyIdQueue.add(hpQueue.remove());
                }
                while (!lpQueue.isEmpty()) {
                    propertyIdQueue.add(lpQueue.remove());
                }
            }
            showRequiredFragment(((FragmentActivity) mContext));
        }
    }

    public static void showRequiredFragment(FragmentActivity activity) {
        PreScreenPopulateProtoResponse preScreenPopulateResponse =  globalPreScreenPopulateResponse;
        Bundle bundle = new Bundle();

        if(propertyIdQueue.size() == 0) {
            Tlog.e("Property Id Queue empty");
            return;
        }
        Tlog.e("Property Queue size: " + propertyIdQueue.size());
        Integer propId = (int) propertyIdQueue.remove();

        Tlog.i("current Property id: " + propId);
        for(Object item : propertyIdQueue){
            Tlog.i(item.toString());
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
                bundle.putBoolean("isFinalFragment", propertyIdQueue.size() == 0);

                document.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, document).commit();
                break;
            case PROPERTY_TYPE_LANGUAGE : // language
                PreScreenLanguage language = new PreScreenLanguage();
                bundle.putByteArray("language", preScreenPopulateResponse.getLanguageList().toByteArray());
                bundle.putBoolean("isFinalFragment", propertyIdQueue.size() == 0);

                language.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, language).commit();
                break;
            case PROPERTY_TYPE_EXPERIENCE : // exp
                PreScreenExperience experience = new PreScreenExperience();
                bundle.putByteArray("experience", preScreenPopulateResponse.getExperience().toByteArray());
                bundle.putBoolean("isFinalFragment", propertyIdQueue.size() == 0);

                experience.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, experience).commit();
                break;
            case PROPERTY_TYPE_EDUCATION : // education
                PreScreenEducation education = new PreScreenEducation();
                bundle.putByteArray("education", preScreenPopulateResponse.getEducation().toByteArray());
                bundle.putLong("jobPostId", preScreenPopulateResponse.getJobPostId());
                bundle.putBoolean("isFinalFragment", propertyIdQueue.size() == 0);


                education.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, education).commit();
                break;
           default:
               PreScreenOthers others = new PreScreenOthers();
               // adding the removed propId back
               propertyIdQueue.add(propId);
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
                       .replace(R.id.pre_screen, others).commit();
               break;
        }
    }
}
