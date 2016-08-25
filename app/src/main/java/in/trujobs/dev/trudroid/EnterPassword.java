package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;

public class EnterPassword extends TruJobsBaseActivity {
    EditText mUserNewPassword;
    private static String EXTRA_TITLE = "Candidate Registration";
    private AsyncTask<LogInRequest, Void, LogInResponse> mAsyncTask;
    ProgressDialog pd;

    public static void resetOldPassword(Context context, String title) {
        Intent intent = new Intent(context, EnterPassword.class);
        EXTRA_TITLE = title;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(EXTRA_TITLE);
        Button mAddPasswordBtn = (Button) findViewById(R.id.add_new_password_btn);

        mAddPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performSavePassword();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Prefs.jobToApplyStatus.put(0);
        Prefs.getJobToApplyJobId.put(0L);
    }

    private void performSavePassword() {
        mUserNewPassword = (EditText) findViewById(R.id.user_new_password_edit_text);

        LogInRequest.Builder requestBuilder = LogInRequest.newBuilder();
        requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
        requestBuilder.setCandidatePassword(mUserNewPassword.getText().toString());

        if(!Util.isValidPassword(requestBuilder.getCandidatePassword())){
            showToast(MessageConstants.ENTER_VALID_PASSWORD);
        } else{
            if (mAsyncTask != null) {
                mAsyncTask.cancel(true);
            }
            mAsyncTask = new LogInRequestAsyncTask();
            mAsyncTask.execute(requestBuilder.build());
        }
    }

    private class LogInRequestAsyncTask extends AsyncTask<LogInRequest,
            Void, LogInResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(EnterPassword.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected LogInResponse doInBackground(LogInRequest... params) {
            return HttpRequest.addPassword(params[0]);
        }

        @Override
        protected void onPostExecute(LogInResponse logInResponse) {
            super.onPostExecute(logInResponse);
            mAsyncTask = null;
            pd.cancel();
            if (logInResponse == null) {
                showToast(MessageConstants.FAILED_REQUEST);
                Tlog.w("Null signIn Response");
                return;
            }

            if (logInResponse.getStatusValue() == ServerConstants.SUCCESS){
                Prefs.firstName.put(logInResponse.getCandidateFirstName());
                Prefs.lastName.put(logInResponse.getCandidateLastName());
                Prefs.candidateGender.put(logInResponse.getCandidateGender());
                Prefs.isAssessed.put(logInResponse.getCandidateIsAssessed());
                Prefs.candidateId.put(logInResponse.getCandidateId());
                Prefs.leadId.put(logInResponse.getLeadId());
                Prefs.candidateMinProfile.put(logInResponse.getMinProfile());
                Prefs.candidateJobPrefStatus.put(logInResponse.getCandidateJobPrefStatus());
                Prefs.candidateHomeLocalityStatus.put(logInResponse.getCandidateHomeLocalityStatus());

                if(Prefs.loginCheckStatus.get() == 1){
                    finish();
                } else{
                    Intent intent;
                    if(Prefs.candidateJobPrefStatus.get() == 0){
                        showToast(MessageConstants.SIGNUP_SUCCESS_PRE_JOB_PREF);
                        intent = new Intent(EnterPassword.this, JobPreference.class);
                    } else if(Prefs.candidateHomeLocalityStatus.get() == 0){
                        showToast(MessageConstants.SIGNUP_SUCCESS_PRE_HOME_LOCALITY);
                        intent = new Intent(EnterPassword.this, HomeLocality.class);
                    } else{
                        intent = new Intent(EnterPassword.this, SearchJobsActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    finish();
                }

            }
            else {
                showToast(MessageConstants.SOMETHING_WENT_WRONG);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
