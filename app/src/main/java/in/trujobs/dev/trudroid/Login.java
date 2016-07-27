package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;

public class Login extends AppCompatActivity {

    private AsyncTask<LogInRequest, Void, LogInResponse> mAsyncTask;
    EditText mMobile;
    EditText mPassword;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        Button loginSubmitBtn = (Button) findViewById(R.id.login_submit_btn);
        TextView forgotPasswordTextView = (TextView) findViewById(R.id.forgot_password_text);
        ImageView loginBackArrow = (ImageView) findViewById(R.id.login_back_arrow);

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        loginBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        loginSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performLogIn();
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
        if(Util.isValidMobile(requestBuilder.getCandidateMobile()) == false){
            Toast.makeText(Login.this, "Enter a valid mobile number",
                    Toast.LENGTH_LONG).show();
            check = 0;
        } else if(Util.isValidPassword(requestBuilder.getCandidatePassword()) == false){
            Toast.makeText(Login.this, "Enter a password of minimum 6 characters",
                    Toast.LENGTH_LONG).show();
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
            pd = new ProgressDialog(Login.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
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
            if (logInResponse == null) {
                Toast.makeText(Login.this, "Failed to Login. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            if(logInResponse.getStatusValue() == ServerConstants.NO_USER){
                Toast.makeText(Login.this, "User does not exist. Please Sign Up!",
                        Toast.LENGTH_LONG).show();
            }

            else if (logInResponse.getStatusValue() == ServerConstants.SUCCESS){
                Toast.makeText(Login.this, "Log In Successful!",
                        Toast.LENGTH_LONG).show();
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
                Intent intent = new Intent(Login.this, JobActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                finish();
            }
            else if (logInResponse.getStatusValue() == ServerConstants.WRONG_PASSWORD) {
                Toast.makeText(Login.this, "Incorrect password. Click \"Forgot Password\" to reset",
                        Toast.LENGTH_LONG).show();
            }

            else {
                Toast.makeText(Login.this, "Something went wrong. Please try again later!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
