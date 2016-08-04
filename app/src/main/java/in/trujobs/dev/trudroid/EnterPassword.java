package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;

public class EnterPassword extends AppCompatActivity {
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

    private void performSavePassword() {
        mUserNewPassword = (EditText) findViewById(R.id.user_new_password_edit_text);

        LogInRequest.Builder requestBuilder = LogInRequest.newBuilder();
        requestBuilder.setCandidateMobile(Prefs.candidateMobile.get().toString());
        requestBuilder.setCandidatePassword(mUserNewPassword.getText().toString());

        if(Util.isValidPassword(requestBuilder.getCandidatePassword()) == false){
            Toast.makeText(EnterPassword.this, "Enter a password of minimum 4 characters",
                    Toast.LENGTH_LONG).show();
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
                Toast.makeText(EnterPassword.this, "Failed to Login. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
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
                        intent = new Intent(EnterPassword.this, JobPreference.class);
                    } else if(Prefs.candidateHomeLocalityStatus.get() == 0){
                        intent = new Intent(EnterPassword.this, HomeLocality.class);
                    } else{
                        intent = new Intent(EnterPassword.this, JobActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                    finish();
                }

            }
            else {
                Toast.makeText(EnterPassword.this, "Something went wrong. Please try again later!",
                        Toast.LENGTH_LONG).show();
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
