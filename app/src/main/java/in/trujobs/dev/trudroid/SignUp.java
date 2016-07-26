package in.trujobs.dev.trudroid;

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
import in.trujobs.proto.SignUpRequest;
import in.trujobs.proto.SignUpResponse;

public class SignUp extends AppCompatActivity {

    private AsyncTask<SignUpRequest, Void, SignUpResponse> mAsyncTask;
    EditText mName;
    EditText mMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button buttonSignupSubmit = (Button) findViewById(R.id.sign_up_submit_btn);
        TextView loginTextView = (TextView) findViewById(R.id.login_text_view);
        ImageView signUpBackArrow = (ImageView) findViewById(R.id.sign_up_back_arrow);

        buttonSignupSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performSignUp();
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
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
        mName = (EditText) findViewById(R.id.sign_up_name_edit_text);
        mMobile = (EditText) findViewById(R.id.sign_up_mobile_edit_text);

        SignUpRequest.Builder requestBuilder = SignUpRequest.newBuilder();
        requestBuilder.setName(mName.getText().toString());
        requestBuilder.setMobile(mMobile.getText().toString());
        Prefs.candidateMobile.put(mMobile.getText().toString());

        int check = 1;

        if(Util.isValidName(requestBuilder.getName()) == false){
            Toast.makeText(SignUp.this, "Enter your Name",
                    Toast.LENGTH_LONG).show();
            check = 0;
        } else if(Util.isValidMobile(requestBuilder.getMobile()) == false) {
            Toast.makeText(SignUp.this, "Enter a valid mobile number",
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
        }

        @Override
        protected SignUpResponse doInBackground(SignUpRequest... params) {
            return HttpRequest.signUpRequest(params[0]);
        }

        @Override
        protected void onPostExecute(SignUpResponse signUpResponse) {
            super.onPostExecute(signUpResponse);
            mAsyncTask = null;
            if (signUpResponse == null) {
                Toast.makeText(SignUp.this, "Failed to Login. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null signIn Response");
                return;
            }

            if(signUpResponse.getStatusValue() == 1){
                Prefs.storedOtp.put(signUpResponse.getGeneratedOtp());
                Intent intent = new Intent(SignUp.this, OtpScreen.class);
                startActivity(intent);
            } else if(signUpResponse.getStatusValue() == 3){
                Toast.makeText(SignUp.this, "Candidate already Exists. Please Login to continue",
                        Toast.LENGTH_LONG).show();
            } else{
                Toast.makeText(SignUp.this, "Something went wrong. Please try again later",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
