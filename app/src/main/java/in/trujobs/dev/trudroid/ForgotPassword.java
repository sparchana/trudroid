package in.trujobs.dev.trudroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        getSupportActionBar().hide();

        ImageView forgotPasswordBackArrow = (ImageView) findViewById(R.id.forgot_password_back_arrow);
        Button buttonGetMobile = (Button) findViewById(R.id.add_mobile_reset_password_btn);

        buttonGetMobile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performPasswordReset();
            }
        });

        forgotPasswordBackArrow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void performPasswordReset() {
        mUserMobile = (EditText) findViewById(R.id.forgot_password_mobile_edit_text);

        ResetPasswordRequest.Builder requestBuilder = ResetPasswordRequest.newBuilder();
        requestBuilder.setMobile(mUserMobile.getText().toString());
        Prefs.candidateMobile.put(mUserMobile.getText().toString());

        int check = 1;
        if(Util.isValidMobile(requestBuilder.getMobile()) == false){
            Toast.makeText(ForgotPassword.this, "Enter a valid mobile number",
                    Toast.LENGTH_LONG).show();
            check = 0;
        }
        if(check == 1){
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
        }

        @Override
        protected ResetPasswordResponse doInBackground(ResetPasswordRequest... params) {
            return HttpRequest.findUserAndSendOtp(params[0]);
        }

        @Override
        protected void onPostExecute(ResetPasswordResponse resetPasswordResponse) {
            super.onPostExecute(resetPasswordResponse);
            mAsyncTask = null;
            if (resetPasswordResponse == null) {
                Toast.makeText(ForgotPassword.this, "Failed to Request. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            if (resetPasswordResponse.getStatusValue() == ServerConstants.SUCCESS){
                Prefs.storedOtp.put(resetPasswordResponse.getOtp());
                Intent intent = new Intent(ForgotPassword.this, OtpScreen.class);
                startActivity(intent);

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
}
