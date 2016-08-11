package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateSkillObject;
import in.trujobs.proto.GetCandidateEducationProfileStaticResponse;
import in.trujobs.proto.GetCandidateExperienceProfileStaticResponse;
import in.trujobs.proto.LanguageKnownObject;
import in.trujobs.proto.LanguageObject;
import in.trujobs.proto.SkillObject;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateExperienceProfileRequest;

/**
 * Created by batcoder1 on 11/8/16.
 */
public class CandidateProfileEducation extends Fragment {

/*    private AsyncTask<UpdateCandidateEducationProfileRequest, Void, UpdateCandidateBasicProfileResponse> UpdateEducationAsyncTask;*/
    private AsyncTask<Void, Void, GetCandidateEducationProfileStaticResponse> mAsyncTask;
    ProgressDialog pd;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
/*        view = inflater.inflate(R.layout.candidate_education_profile, container, false);*/

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

/*        mAsyncTask = new GetEducationStaticAsyncTask();
        mAsyncTask.execute();*/

        Button saveExperienceBtn = (Button) view.findViewById(R.id.save_experience_btn);
        saveExperienceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }

/*    private class UpdateEducationProfileAsyncTask extends AsyncTask<UpdateCandidateEducationProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected UpdateCandidateBasicProfileResponse doInBackground(UpdateCandidateEducationProfileRequest ... params) {
            return HttpRequest.updateCandidateEducationProfile(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse) {
            super.onPostExecute(updateCandidateBasicProfileResponse);
            pd.cancel();

        }
    }

    private class GetEducationStaticAsyncTask extends AsyncTask<Void,
            Void, GetCandidateEducationProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected GetCandidateEducationProfileStaticResponse doInBackground(Void... params) {
            return HttpRequest.getCandidateEducationProfileStatic();
        }

        @Override
        protected void onPostExecute(GetCandidateExperienceProfileStaticResponse getCandidateExperienceProfileStaticResponse) {
            super.onPostExecute(getCandidateExperienceProfileStaticResponse);
            pd.cancel();

            if (getCandidateExperienceProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Something went wrong in fetching data",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {

            }
        }
    }*/

}

