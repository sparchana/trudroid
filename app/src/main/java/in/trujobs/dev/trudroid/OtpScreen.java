package in.trujobs.dev.trudroid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    static EditText mUserOtpOne;
    static EditText mUserOtpTwo;
    static EditText mUserOtpThree;
    static EditText mUserOtpFour;
    private AsyncTask<ResetPasswordRequest, Void, ResetPasswordResponse> mAsyncTask;
    ProgressDialog pd;
    private IntentFilter mIntentFilter;
    private IncomingSms mIncomingSms;

    private Integer PERMISSIONS_REQUEST_RECEIVE_SMS = 1;
    public static void resetPassword(Context context, String title) {
        Intent intent = new Intent(context, OtpScreen.class);
        EXTRA_TITLE = title;
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mIncomingSms, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mIncomingSms);
    }

    public static class IncomingSms extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Tlog.e("received Sms");
            final Bundle bundle = intent.getExtras();
            try {
                if (bundle != null) {
                    final Object[] pdusObj = (Object[]) bundle.get("pdus");

                    for (int i = 0; i < pdusObj.length; i++) {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();
                        Pattern pattern = Pattern.compile("(\\d{4})");
                        Matcher matcher = pattern.matcher(message);
                        //checking if it form trujobs
                        if(phoneNumber.equals("MD-TRUJOB")){
                            String val = "";
                            if (matcher.find()) {
                                val = matcher.group(1);
                                Tlog.e("received otp: " + val.charAt(0) + val.charAt(1)+ val.charAt(2) + val.charAt(3));
                                char firstChar = val.charAt(0);
                                char secondChar = val.charAt(1);
                                char thirdChar = val.charAt(2);
                                char fourChar = val.charAt(3);

                                //setting individual otps in the respective order
                                mUserOtpOne.setText(firstChar + "");
                                mUserOtpTwo.setText(secondChar + "");
                                mUserOtpThree.setText(thirdChar + "");
                                mUserOtpFour.setText(fourChar + "");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SmsReceiver", "Exception smsReceiver" + e);

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_screen);
        setTitle(EXTRA_TITLE);

        mUserOtpOne = (EditText) findViewById(R.id.user_otp_first_edit_text);
        mUserOtpTwo = (EditText) findViewById(R.id.user_otp_second_edit_text);
        mUserOtpThree = (EditText) findViewById(R.id.user_otp_third_edit_text);
        mUserOtpFour = (EditText) findViewById(R.id.user_otp_fourth_edit_text);
        Button addPasswordBtn = (Button) findViewById(R.id.add_password_btn);
        Button resendOtpBtn = (Button) findViewById(R.id.resend_otp_btn);

        mIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        mIncomingSms = new IncomingSms();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OtpScreen.this,
                    new String[]{Manifest.permission.RECEIVE_SMS},
                    PERMISSIONS_REQUEST_RECEIVE_SMS);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                if (s.length() == 1) {}
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
