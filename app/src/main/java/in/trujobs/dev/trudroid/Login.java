package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;
import in.trujobs.proto.UpdateTokenRequest;
import in.trujobs.proto.UpdateTokenResponse;

public class Login extends TruJobsBaseActivity {

    private AsyncTask<LogInRequest, Void, LogInResponse> mAsyncTask;
    private AsyncTask<UpdateTokenRequest, Void, UpdateTokenResponse> mUpdateTokenAsyncTask;
    EditText mMobile;
    EditText mPassword;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        // Track screen view
        addScreenViewGA(Constants.GA_SCREEN_NAME_LOGIN);

        Button loginSubmitBtn = (Button) findViewById(R.id.login_submit_btn);
        TextView forgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_text);
        TextView alreadyAUserTextView = (TextView) findViewById(R.id.already_user);
        ImageView loginBackArrow = (ImageView) findViewById(R.id.login_back_arrow);

        mMobile = (EditText) findViewById(R.id.user_mobile_edit_text);

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                // Pass the mobile number entered by user to the forgot password activity
                intent.putExtra(Constants.FORGOT_PWD_MOBILE_EXTRA, mMobile.getText().toString());
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        alreadyAUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignUp.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        pd = CustomProgressDialog.get(Login.this);

        loginBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

                // Track action
                addActionGA(Constants.GA_SCREEN_NAME_LOGIN, Constants.GA_ACTION_BACKED_FROM_LOGIN);
            }
        });

        loginSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogIn();

                // Track action
                addActionGA(Constants.GA_SCREEN_NAME_LOGIN, Constants.GA_ACTION_LOGIN);
            }
        });
    }

    private void performLogIn() {

        mMobile = (EditText) findViewById(R.id.user_mobile_edit_text);
        mPassword = (EditText) findViewById(R.id.user_password_edit_text);

        LogInRequest.Builder requestBuilder = LogInRequest.newBuilder();
        requestBuilder.setCandidateMobile(mMobile.getText().toString());
        requestBuilder.setCandidatePassword(mPassword.getText().toString());

        int check = 1;
        if(!Util.isValidMobile(requestBuilder.getCandidateMobile())){
            showToast(MessageConstants.ENTER_VALID_MOBILE);
            check = 0;
        } else if(!Util.isValidPassword(requestBuilder.getCandidatePassword())){
            showToast(MessageConstants.ENTER_VALID_PASSWORD);
            check = 0;
        }
        if(check == 1){
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
            pd.show();
        }

        @Override
        protected LogInResponse doInBackground(LogInRequest... params) {
            return HttpRequest.loginRequest(params[0]);
        }

        @Override
        protected void onPostExecute(LogInResponse logInResponse) {
            super.onPostExecute(logInResponse);
            mAsyncTask = null;
            pd.cancel();
            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (logInResponse == null) {
                showToast(MessageConstants.FAILED_REQUEST);
                Log.w("","Null signIn Response");
                return;
            }

            if(logInResponse.getStatusValue() == ServerConstants.NO_USER){
                showToast(MessageConstants.NO_USER);
            }

            else if (logInResponse.getStatusValue() == ServerConstants.SUCCESS){
                showToast("Log In Successful!");

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPassword.getWindowToken(), 0);

                Prefs.candidateMobile.put(mMobile.getText().toString());
                Prefs.firstName.put(logInResponse.getCandidateFirstName());
                Prefs.lastName.put(logInResponse.getCandidateLastName());
                Prefs.candidateGender.put(logInResponse.getCandidateGender());
                Prefs.isAssessed.put(logInResponse.getCandidateIsAssessed());
                Prefs.candidateId.put(logInResponse.getCandidateId());
                Prefs.leadId.put(logInResponse.getLeadId());
                Prefs.candidateMinProfile.put(logInResponse.getMinProfile());
                Prefs.sessionId.put(logInResponse.getSessionId());
                Prefs.sessionExpiry.put(logInResponse.getSessionExpiryMillis());
                Prefs.candidateJobPrefStatus.put(logInResponse.getCandidateJobPrefStatus());
                Prefs.candidateHomeLocalityStatus.put(logInResponse.getCandidateHomeLocalityStatus());
                Prefs.candidateHomeLat.put(String.valueOf(logInResponse.getCandidateHomeLatitude()));
                Prefs.candidateHomeLng.put(String.valueOf(logInResponse.getCandidateHomeLongitude()));
                Prefs.candidatePrefJobRoleIdOne.put(logInResponse.getCandidatePrefJobRoleIdOne());
                Prefs.candidatePrefJobRoleIdTwo.put(logInResponse.getCandidatePrefJobRoleIdTwo());
                Prefs.candidatePrefJobRoleIdThree.put(logInResponse.getCandidatePrefJobRoleIdThree());
                Prefs.candidateHomeLocalityName.put(logInResponse.getCandidateHomeLocalityName());
                /* TODO find a better way to clear jobFilterbkp on login */
                SearchJobsActivity.jobFilterRequestBkp = null;

                Tlog.e(checkPlayServices() + " ------------------------------------");
                //setting and generating token
                if(FirebaseInstanceId.getInstance().getToken() != null){
                    //generating token
                    FirebaseInstanceId.getInstance().getToken();
                    Tlog.e("New token: " + FirebaseInstanceId.getInstance().getToken());

                    //saving in prefs
                    Prefs.fcmToken.put(FirebaseInstanceId.getInstance().getToken());

                    //update candidate token
                    UpdateTokenRequest.Builder requestBuilder = UpdateTokenRequest.newBuilder();
                    requestBuilder.setCandidateId(String.valueOf(logInResponse.getCandidateId()));
                    requestBuilder.setToken(Prefs.fcmToken.get());

                    if (mUpdateTokenAsyncTask != null) {
                        mUpdateTokenAsyncTask.cancel(true);
                    }
                    mUpdateTokenAsyncTask = new UpdateTokenRequestAsyncTask();
                    mUpdateTokenAsyncTask.execute(requestBuilder.build());
                } else{
                    Intent intent;
                    if(Prefs.candidateJobPrefStatus.get() == 0){
                        intent = new Intent(Login.this, JobPreference.class);
                    } else if(Prefs.candidateHomeLocalityStatus.get() == 0){
                        intent = new Intent(Login.this, HomeLocality.class);
                    } else{
                        intent = new Intent(Login.this, SearchJobsActivity.class);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    finish();
                }
            } else if (logInResponse.getStatusValue() == ServerConstants.WRONG_PASSWORD) {
                showToast(MessageConstants.INCORRECT_PASSWORD);
            }

            else {
                showToast(MessageConstants.SOMETHING_WENT_WRONG);
            }
        }
    }


    private class UpdateTokenRequestAsyncTask extends AsyncTask<UpdateTokenRequest,
            Void, UpdateTokenResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected UpdateTokenResponse doInBackground(UpdateTokenRequest... params) {
            return HttpRequest.updateTokenRequest(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateTokenResponse updateTokenResponse) {
            super.onPostExecute(updateTokenResponse);
            mUpdateTokenAsyncTask = null;
            pd.cancel();
            if(!Util.isConnectedToInternet(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
                return;
            } else if (updateTokenResponse == null) {
                showToast(MessageConstants.FAILED_REQUEST);
                Log.w("","Null signIn Response");
                return;
            }

            Intent intent;
            if(Prefs.candidateJobPrefStatus.get() == 0){
                intent = new Intent(Login.this, JobPreference.class);
            } else if(Prefs.candidateHomeLocalityStatus.get() == 0){
                intent = new Intent(Login.this, HomeLocality.class);
            } else{
                intent = new Intent(Login.this, SearchJobsActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            finish();
        }
    }

}
