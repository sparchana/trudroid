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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateSkillObject;
import in.trujobs.proto.DegreeObject;
import in.trujobs.proto.EducationObject;
import in.trujobs.proto.GetCandidateEducationProfileStaticResponse;
import in.trujobs.proto.GetCandidateExperienceProfileStaticResponse;
import in.trujobs.proto.LanguageKnownObject;
import in.trujobs.proto.LanguageObject;
import in.trujobs.proto.SkillObject;
import in.trujobs.proto.TimeShiftObject;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateEducationProfileRequest;
import in.trujobs.proto.UpdateCandidateExperienceProfileRequest;

/**
 * Created by batcoder1 on 11/8/16.
 */
public class CandidateProfileEducation extends Fragment {

    private AsyncTask<UpdateCandidateEducationProfileRequest, Void, UpdateCandidateBasicProfileResponse> UpdateEducationAsyncTask;
    private AsyncTask<Void, Void, GetCandidateEducationProfileStaticResponse> mAsyncTask;
    ProgressDialog pd;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.candidate_education_profile, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

        mAsyncTask = new GetEducationStaticAsyncTask();
        mAsyncTask.execute();

        return view;
    }

    private class UpdateEducationProfileAsyncTask extends AsyncTask<UpdateCandidateEducationProfileRequest,
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
        protected void onPostExecute(GetCandidateEducationProfileStaticResponse getCandidateEducationProfileStaticResponse) {
            super.onPostExecute(getCandidateEducationProfileStaticResponse);
            pd.cancel();

            if (getCandidateEducationProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Something went wrong in fetching data",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {

                Log.e("edustatic : ", "data:-- " + getCandidateEducationProfileStaticResponse);
                final Spinner educationLevel = (Spinner) view.findViewById(R.id.candidate_qualification);
                final Spinner degree = (Spinner) view.findViewById(R.id.candidate_degree);

                List<String> eduLevel = new ArrayList<String>();
                List<String> degreeName = new ArrayList<String>();

                final List<Integer> eduLevelId = new ArrayList<Integer>();
                final List<Integer> degreeId = new ArrayList<Integer>();

                for(EducationObject educationObject : getCandidateEducationProfileStaticResponse.getEducationObjectList()){
                    eduLevel.add(educationObject.getEducationName());
                    eduLevelId.add((int) educationObject.getEducationId());
                }

                for(DegreeObject degreeObject : getCandidateEducationProfileStaticResponse.getDegreeObjectList()){
                    degreeName.add(degreeObject.getDegreeName());
                    degreeId.add((int) degreeObject.getDegreeId());
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, eduLevel);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                educationLevel.setAdapter(spinnerAdapter);

                spinnerAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, degreeName);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                degree.setAdapter(spinnerAdapter);
            }
        }
    }
}

