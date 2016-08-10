package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.CandidateInformationRequest;
import in.trujobs.proto.GetCandidateBasicProfileStaticResponse;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.TimeShiftObject;
import in.trujobs.proto.UpdateCandidateBasicProfileRequest;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;

/**
 * Created by batcoder1 on 2/8/16.
 */
public class CandidateProfileBasic extends Fragment {

    private AsyncTask<Void, Void, GetCandidateBasicProfileStaticResponse> mAsyncTask;
    private AsyncTask<UpdateCandidateBasicProfileRequest, Void, UpdateCandidateBasicProfileResponse> mSaveBasicProfileAsyncTask;
    ProgressDialog pd;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.candidate_basic_profile, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

        mAsyncTask = new GetBasicStaticAsyncTask();
        mAsyncTask.execute();

        return view;
    }

    private class GetBasicStaticAsyncTask extends AsyncTask<Void,
    Void, GetCandidateBasicProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
            }

        @Override
        protected GetCandidateBasicProfileStaticResponse doInBackground(Void... params) {
            return HttpRequest.getCandidateBasicProfileStatic();
        }

        @Override
        protected void onPostExecute(GetCandidateBasicProfileStaticResponse getCandidateBasicProfileStaticResponse) {
            super.onPostExecute(getCandidateBasicProfileStaticResponse);
            pd.cancel();

            if (getCandidateBasicProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Null Candidate returned",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null candidate Response");
                return;
            } else {
                Spinner shift_option = (Spinner) view.findViewById(R.id.shift_option);
                //shift_option.setOnItemSelectedListener();

                EditText jobPrefLocation = (EditText) view.findViewById(R.id.pref_location);
                // Job preferred location
                jobPrefLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                // Job preferred location next button
                Button saveBasicProfileBtn = (Button) view.findViewById(R.id.button_save_basic);
                saveBasicProfileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditText mFirstName = (EditText) view.findViewById(R.id.first_name);
                        EditText mLastName = (EditText) view.findViewById(R.id.last_name);

                        UpdateCandidateBasicProfileRequest.Builder requestBuilder = UpdateCandidateBasicProfileRequest.newBuilder();
                        requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                        requestBuilder.setCandidateFirstName("Adarsh");
                        requestBuilder.setCandidateLastName("Raj");

                        mSaveBasicProfileAsyncTask = new UpdateBasicProfileAsyncTask();
                        mSaveBasicProfileAsyncTask.execute(requestBuilder.build());
                    }
                });

                List<String> categories = new ArrayList<String>();

                for(TimeShiftObject timeShiftObject : getCandidateBasicProfileStaticResponse.getTimeShiftListList()){
                   categories.add(timeShiftObject.getTimeShiftName());
                }

                ArrayAdapter<String> shift_adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, categories);

                shift_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                shift_option.setAdapter(shift_adapter);
            }
        }
    }

    private class UpdateBasicProfileAsyncTask extends AsyncTask<UpdateCandidateBasicProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getContext(),R.style.SpinnerTheme);
            pd.setCancelable(false);
            pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
            pd.show();
        }

        @Override
        protected UpdateCandidateBasicProfileResponse doInBackground(UpdateCandidateBasicProfileRequest... params) {
            return HttpRequest.updateCandidateBasicProfile(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse) {
            super.onPostExecute(updateCandidateBasicProfileResponse);
            pd.cancel();

            if (updateCandidateBasicProfileResponse == null) {
                Toast.makeText(getContext(), "Something went Wrong. Please try again!",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null update Response");
                return;
            } else {
                CandidateProfileExperience candidateProfileExperience = new CandidateProfileExperience();
                candidateProfileExperience.setArguments(getActivity().getIntent().getExtras());
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_profile, candidateProfileExperience).commit();
            }
        }
    }
}
