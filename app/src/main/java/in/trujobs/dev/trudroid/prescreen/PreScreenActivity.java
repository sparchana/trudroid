package in.trujobs.dev.trudroid.prescreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import in.trujobs.dev.trudroid.Helper.ApplyJobResponseBundle;
import in.trujobs.dev.trudroid.JobDetailActivity;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.SearchJobsActivity;
import in.trujobs.dev.trudroid.TruJobsBaseActivity;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.LocalityObject;
import in.trujobs.proto.PreScreenPopulateProtoRequest;
import in.trujobs.proto.PreScreenPopulateProtoResponse;

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

            Tlog.i("size: " + preScreenPopulateResponse.getPropertyIdList().size());
            if(preScreenPopulateResponse.getPropertyIdCount() > 0) {
                for(Integer jobPostId : preScreenPopulateResponse.getPropertyIdList()){
                    if(!propertyIdQueue.contains(jobPostId)){
                        propertyIdQueue.add(jobPostId);
                    }
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
        Tlog.e("Property Queue size" + propertyIdQueue.size());
        Integer propId = (int) propertyIdQueue.remove();

        Tlog.i("current Property id " + propId);
        for(Object item : propertyIdQueue){
            Tlog.i(item.toString());
        }
        switch (propId) {
            case 0 : // documents
                bundle = new Bundle();
                PreScreenDocument document = new PreScreenDocument();
                bundle.putByteArray("document", preScreenPopulateResponse.getDocumentList().toByteArray());

                document.setArguments(bundle);
                // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, document).commit();
                break;
            case 1 : // language
                PreScreenLanguage language = new PreScreenLanguage();
                bundle.putByteArray("language", preScreenPopulateResponse.getLanguageList().toByteArray());

                language.setArguments(bundle);
                // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, language).commit();
                break;
            case 2 : // asset

                break;
            case 3 : // age
                break;
            case 4 : // exp
                PreScreenExperience experience = new PreScreenExperience();
                bundle.putByteArray("experience", preScreenPopulateResponse.getExperience().toByteArray());

                experience.setArguments(bundle);
                // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, experience).commit();
                break;
            case 5 : // education
                PreScreenEducation education = new PreScreenEducation();
                bundle.putByteArray("education", preScreenPopulateResponse.getEducation().toByteArray());

                education.setArguments(bundle);
                // Add the fragment to the 'overlay_job_filter_fragment_container' FrameLayout
                activity.getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                        .replace(R.id.pre_screen, education).commit();
                break;
            case 6 : break;
        }
    }
}
