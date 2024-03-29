package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.proto.SignUpRequest;
import in.trujobs.proto.SignUpResponse;

public class SignUp extends TruJobsBaseActivity {

    private AsyncTask<SignUpRequest, Void, SignUpResponse> mAsyncTask;
    EditText mName;
    EditText mMobile;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        // track in GA
        addScreenViewGA(Constants.GA_SCREEN_NAME_SIGNUP);

        Button buttonSignupSubmit = (Button) findViewById(R.id.sign_up_submit_btn);
        TextView loginTextView = (TextView) findViewById(R.id.login_text_view);
        ImageView signUpBackArrow = (ImageView) findViewById(R.id.sign_up_back_arrow);

        buttonSignupSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performSignUp();
            }
        });

        pd = CustomProgressDialog.get(SignUp.this);

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class);
                finish();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            }
        });

        signUpBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void performSignUp() {

        // Track this action
        addActionGA(Constants.GA_SCREEN_NAME_SIGNUP, Constants.GA_ACTION_SIGNUP);

        mName = (EditText) findViewById(R.id.sign_up_name_edit_text);
        mMobile = (EditText) findViewById(R.id.sign_up_mobile_edit_text);

        SignUpRequest.Builder requestBuilder = SignUpRequest.newBuilder();
        requestBuilder.setName(mName.getText().toString());
        requestBuilder.setMobile(mMobile.getText().toString());
        Prefs.candidateMobile.put(mMobile.getText().toString());

        int check = 1;

        if(Util.isValidName(requestBuilder.getName()) == 0){
            Toast.makeText(SignUp.this, MessageConstants.ENTER_NAME,
                    Toast.LENGTH_LONG).show();
            check = 0;
        } else if(Util.isValidName(requestBuilder.getName()) == 1) {
            Toast.makeText(SignUp.this, MessageConstants.ENTER_VALID_NAME,
                    Toast.LENGTH_LONG).show();
            check = 0;
        } else if(!Util.isValidMobile(requestBuilder.getMobile())) {
            Toast.makeText(SignUp.this, MessageConstants.ENTER_VALID_MOBILE,
                    Toast.LENGTH_LONG).show();
            check = 0;
        }

        if(check == 1){
            if (mAsyncTask != null) {
                mAsyncTask.cancel(true);
            }
            mAsyncTask = new SignUpRequestAsyncTask();
            mAsyncTask.execute(requestBuilder.build());
        }

    }

    private class SignUpRequestAsyncTask extends AsyncTask<SignUpRequest,
            Void, SignUpResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected SignUpResponse doInBackground(SignUpRequest... params) {
            return HttpRequest.signUpRequest(params[0]);
        }

        @Override
        protected void onPostExecute(SignUpResponse signUpResponse) {
            super.onPostExecute(signUpResponse);
            mAsyncTask = null;
            pd.cancel();
            if (signUpResponse == null) {
                Toast.makeText(SignUp.this, MessageConstants.FAILED_REQUEST,
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            if(signUpResponse.getStatusValue() == 1){
                Prefs.storedOtp.put(signUpResponse.getGeneratedOtp());
                Intent intent = new Intent(SignUp.this, OtpScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
            } else if(signUpResponse.getStatusValue() == 3){
                Toast.makeText(SignUp.this, MessageConstants.EXISTING_USER,
                        Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(SignUp.this, MessageConstants.SOMETHING_WENT_WRONG,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
