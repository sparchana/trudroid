package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.ResetPasswordRequest;
import in.trujobs.proto.ResetPasswordResponse;

public class ForgotPassword extends TruJobsBaseActivity {

    private AsyncTask<ResetPasswordRequest, Void, ResetPasswordResponse> mAsyncTask;
    EditText mUserMobile;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // track screen view
        addScreenViewGA(Constants.GA_SCREEN_NAME_FORGOT_PASSWORD);

        // Fetch the mobile number entered by user in the login activity and set it in the text view
        mUserMobile = (EditText) findViewById(R.id.forgot_password_mobile_edit_text);
        String forgotPwdMobile = getIntent().getStringExtra(Constants.FORGOT_PWD_MOBILE_EXTRA);

        if (forgotPwdMobile != null && !forgotPwdMobile.isEmpty()) {
            mUserMobile.setText(forgotPwdMobile);
        }

        pd = CustomProgressDialog.get(ForgotPassword.this);

        Button buttonGetMobile = (Button) findViewById(R.id.add_mobile_reset_password_btn);
        buttonGetMobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performPasswordReset();

                //Track this action
                addActionGA(Constants.GA_SCREEN_NAME_FORGOT_PASSWORD, Constants.GA_ACTION_SAVE_PASSWORD);
            }
        });
    }

    private void performPasswordReset() {
        mUserMobile = (EditText) findViewById(R.id.forgot_password_mobile_edit_text);

        ResetPasswordRequest.Builder requestBuilder = ResetPasswordRequest.newBuilder();
        requestBuilder.setMobile(mUserMobile.getText().toString());
        Prefs.candidateMobile.put(mUserMobile.getText().toString());
        requestBuilder.setAppVersionCode(ServerConstants.CURRENT_APP_VERSION);

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
                Tlog.w("Null signIn Response");
                return;
            }

            if (resetPasswordResponse.getStatusValue() == ServerConstants.SUCCESS){
                Prefs.storedOtp.put(resetPasswordResponse.getOtp());
                OtpScreen.resetPassword(ForgotPassword.this, "Reset Password");

            } else if (resetPasswordResponse.getStatusValue() == ServerConstants.NO_USER_TO_SEND_OTP){
                Toast.makeText(ForgotPassword.this, MessageConstants.NO_USER,
                        Toast.LENGTH_LONG).show();
            } else if (resetPasswordResponse.getStatusValue() == ServerConstants.NO_AUTH) {
                showToast(MessageConstants.NO_AUTH);
            } else {
                Toast.makeText(ForgotPassword.this, MessageConstants.SOMETHING_WENT_WRONG,
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
