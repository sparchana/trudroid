package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.ResetPasswordRequest;
import in.trujobs.proto.ResetPasswordResponse;

public class OtpScreen extends TruJobsBaseActivity {
    private static String EXTRA_TITLE = "Candidate Registration";
    EditText mUserOtpOne, mUserOtpTwo, mUserOtpThree, mUserOtpFour;
    private AsyncTask<ResetPasswordRequest, Void, ResetPasswordResponse> mAsyncTask;
    ProgressDialog pd;

    public static void resetPassword(Context context, String title) {
        Intent intent = new Intent(context, OtpScreen.class);
        EXTRA_TITLE = title;
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_screen);
        setTitle(EXTRA_TITLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserOtpOne = (EditText) findViewById(R.id.user_otp_first_edit_text);
        mUserOtpTwo = (EditText) findViewById(R.id.user_otp_second_edit_text);
        mUserOtpThree = (EditText) findViewById(R.id.user_otp_third_edit_text);
        mUserOtpFour = (EditText) findViewById(R.id.user_otp_fourth_edit_text);
        Button addPasswordBtn = (Button) findViewById(R.id.add_password_btn);
        Button resendOtpBtn = (Button) findViewById(R.id.resend_otp_btn);

        pd = CustomProgressDialog.get(OtpScreen.this);

        resendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendOtp();
            }
        });

        mUserOtpOne.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() ==1) {
                    mUserOtpTwo.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        mUserOtpTwo.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    mUserOtpThree.requestFocus();
                }
                else if (s.length() == 0) {
                    mUserOtpOne.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        mUserOtpThree.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    mUserOtpFour.requestFocus();
                }
                else if (s.length() == 0) {
                    mUserOtpTwo.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        mUserOtpFour.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    checkOtp();
                }
                else if (s.length() == 0) {
                    mUserOtpThree.requestFocus();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}
        });

        addPasswordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkOtp();
            }
        });
    }

    public void checkOtp(){
        if(mUserOtpOne.getText().length() == 0 || mUserOtpTwo.getText().length() == 0 || mUserOtpThree.getText().length() == 0 || mUserOtpFour.getText().length() == 0) {
            Toast.makeText(OtpScreen.this, "Please enter the OTP",
                    Toast.LENGTH_LONG).show();
        } else{
            String userOtp;
            userOtp = mUserOtpOne.getText().toString() + mUserOtpTwo.getText().toString() + mUserOtpThree.getText().toString() + mUserOtpFour.getText().toString();
            if(userOtp.equals(Prefs.storedOtp.get().toString())){
                EnterPassword.resetOldPassword(OtpScreen.this, EXTRA_TITLE);
            } else{
                Toast.makeText(OtpScreen.this, "Oops! Incorrect OTP",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void resendOtp(){
        ResetPasswordRequest.Builder requestBuilder = ResetPasswordRequest.newBuilder();
        requestBuilder.setMobile(Prefs.candidateMobile.get());

        mAsyncTask = new ResendOtpRequestAsyncTask();
        mAsyncTask.execute(requestBuilder.build());
    }

    private class ResendOtpRequestAsyncTask extends AsyncTask<ResetPasswordRequest,
            Void, ResetPasswordResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected ResetPasswordResponse doInBackground(ResetPasswordRequest... params) {
            return HttpRequest.resendOtp(params[0]);
        }

        @Override
        protected void onPostExecute(ResetPasswordResponse resetPasswordResponse) {
            super.onPostExecute(resetPasswordResponse);
            mAsyncTask = null;
            pd.cancel();
            if (resetPasswordResponse == null) {
                Toast.makeText(OtpScreen.this, MessageConstants.FAILED_REQUEST,
                        Toast.LENGTH_LONG).show();
                Tlog.w("Null resend otp Response");
                return;
            }

            if (resetPasswordResponse.getStatus() == ResetPasswordResponse.Status.SUCCESS){
                Prefs.storedOtp.put(resetPasswordResponse.getOtp());
                Toast.makeText(OtpScreen.this, "OTP Resent!",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(OtpScreen.this, MessageConstants.SOMETHING_WENT_WRONG,
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
