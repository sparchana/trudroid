package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import in.trujobs.proto.ResetPasswordRequest;
import in.trujobs.proto.ResetPasswordResponse;

public class ForgotPassword extends AppCompatActivity {

    private AsyncTask<ResetPasswordRequest, Void, ResetPasswordResponse> mAsyncTask;
    EditText mUserMobile;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button buttonGetMobile = (Button) findViewById(R.id.add_mobile_reset_password_btn);
        buttonGetMobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performPasswordReset();
            }
        });
    }

    private void performPasswordReset() {
        mUserMobile = (EditText) findViewById(R.id.forgot_password_mobile_edit_text);

        ResetPasswordRequest.Builder requestBuilder = ResetPasswordRequest.newBuilder();
        requestBuilder.setMobile(mUserMobile.getText().toString());
        Prefs.candidateMobile.put(mUserMobile.getText().toString());

        if(Util.isValidMobile(requestBuilder.getMobile()) == false){
            Toast.makeText(ForgotPassword.this, "Enter a valid 10 digit mobile number",
                    Toast.LENGTH_LONG).show();
        } else {
            if (mAsyncTask != null) {
                mAsyncTask.cancel(true);
            }
            mAsyncTask = new ResetPasswordRequestAsyncTask();
            mAsyncTask.execute(requestBuilder.build());
        }
    }

    private class ResetPasswordRequestAsyncTask extends AsyncTask<ResetPasswordRequest,
            Void, ResetPasswordResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(ForgotPassword.this,R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected ResetPasswordResponse doInBackground(ResetPasswordRequest... params) {
            return HttpRequest.findUserAndSendOtp(params[0]);
        }

        @Override
        protected void onPostExecute(ResetPasswordResponse resetPasswordResponse) {
            super.onPostExecute(resetPasswordResponse);
            mAsyncTask = null;
            pd.cancel();
            if (resetPasswordResponse == null) {
                Toast.makeText(ForgotPassword.this, "Failed to Request. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            if (resetPasswordResponse.getStatusValue() == ServerConstants.SUCCESS){
                Prefs.storedOtp.put(resetPasswordResponse.getOtp());
                OtpScreen.resetPassword(ForgotPassword.this, "Reset Password");

            } else if (resetPasswordResponse.getStatusValue() == ServerConstants.NO_USER_TO_SEND_OTP){
                Toast.makeText(ForgotPassword.this, "Account doesn't exists!",
                        Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(ForgotPassword.this, "Something went wrong. Please try again later!",
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
