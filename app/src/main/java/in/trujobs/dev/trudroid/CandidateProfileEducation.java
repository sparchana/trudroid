package in.trujobs.dev.trudroid;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.GetCandidateEducationProfileStaticResponse;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateEducationProfileRequest;

/**
 * Created by batcoder1 on 11/8/16.
 */
public class CandidateProfileEducation extends Fragment {

    private AsyncTask<UpdateCandidateEducationProfileRequest, Void, UpdateCandidateBasicProfileResponse> UpdateEducationAsyncTask;
    private AsyncTask<Void, Void, GetCandidateEducationProfileStaticResponse> mAsyncTask;
    ProgressDialog pd;
    LinearLayout degreeSection;
    EditText candidateCollege;
    Button updateEducationProfile;

    int qualificationPos = 0, degreePos = 0;
    View view;

    SpinnerAdapter adapter;

    public CandidateInfoActivity candidateInfoActivity;
    String[] qualificationLevel = new String[0];
    Long[] qualificationId = new Long[0];

    String[] degreeName = new String[0];
    Long[] degreeId = new Long[0];

    //values
    private Long qualificationSelected = Long.valueOf(0), degreeSelected = Long.valueOf(0);
    Integer qualificationStatus = -1;
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
                candidateInfoActivity = (CandidateInfoActivity) getActivity();

                updateEducationProfile = (Button) view.findViewById(R.id.save_education);
                final Spinner candidateQualification = (Spinner) view.findViewById(R.id.candidate_qualification);
                final Spinner candidateDegree = (Spinner) view.findViewById(R.id.candidate_degree);

                qualificationLevel = new String[getCandidateEducationProfileStaticResponse.getEducationObjectCount() + 1];
                qualificationId = new Long[getCandidateEducationProfileStaticResponse.getEducationObjectCount() + 1];

                degreeName = new String[getCandidateEducationProfileStaticResponse.getDegreeObjectCount() + 1];
                degreeId = new Long[getCandidateEducationProfileStaticResponse.getDegreeObjectCount() + 1];

                qualificationLevel[0] = "Select Education Level";
                qualificationId[0] = Long.valueOf(-1);
                for(int i = 1; i<= getCandidateEducationProfileStaticResponse.getEducationObjectCount() ; i++){
                    qualificationLevel[i] = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationName();
                    qualificationId[i] = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId();
                    if(getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId() == candidateInfoActivity.candidateInfo.getCandidate().getCandidateEducation().getEducation().getEducationId()){
                        qualificationSelected = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId();
                        qualificationPos = i;
                    }
                }

                degreeName[0] = "Select Highest Degree";
                degreeId[0] = Long.valueOf(-1);
                for(int i = 1; i<= getCandidateEducationProfileStaticResponse.getDegreeObjectCount() ; i++){
                    degreeName[i] = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeName();
                    degreeId[i] = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId();
                    if(getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId() == candidateInfoActivity.candidateInfo.getCandidate().getCandidateEducation().getDegree().getDegreeId()){
                        degreeSelected = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId();
                        degreePos = i;
                    }
                }

                adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, qualificationLevel, 0);
                candidateQualification.setAdapter(adapter);

                candidateCollege = (EditText) view.findViewById(R.id.candidate_college);

                degreeSection = (LinearLayout) view.findViewById(R.id.degree_section);
                degreeSection.setVisibility(View.GONE);

                adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, degreeName, 2);
                candidateDegree.setAdapter(adapter);

                //prefilling data
                candidateQualification.setSelection(qualificationPos);
                candidateDegree.setSelection(degreePos);

                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateEducation().getCandidateInstitute() != null){
                    candidateCollege.setText(candidateInfoActivity.candidateInfo.getCandidate().getCandidateEducation().getCandidateInstitute());
                }

                qualificationStatus = candidateInfoActivity.candidateInfo.getCandidate().getCandidateEducation().getCandidateEducationCompletionStatus();
                candidateQualification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                    {
                        if(position != 0){
                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setMessage("Are you " + qualificationLevel[position] + " pass?");
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            qualificationStatus = 1;
                                            ImageView imageView = (ImageView) view.findViewById(R.id.spinnerImagesQualification);
                                            imageView.setImageResource(R.drawable.tick);
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            qualificationStatus = 0;
                                            ImageView imageView = (ImageView) view.findViewById(R.id.spinnerImagesQualification);
                                            imageView.setImageResource(R.drawable.wrong);
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }

                        if(position > 3){
                            degreeSection.setVisibility(View.VISIBLE);
                        } else {
                            degreeSection.setVisibility(View.GONE);
                        }
                        qualificationSelected = qualificationId[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {}
                });

                candidateDegree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                    {
                        degreeSelected = degreeId[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {}
                });

                updateEducationProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean check = true;
                        if(qualificationSelected == 0){
                            System.out.println("111 ==============");
                            check = false;
                        } else if(degreeSelected == 0){
                            check = false;
                            System.out.println("222 ==============");
                        } else if(qualificationSelected != 0 && degreeSelected == 0){
                            check = false;
                            System.out.println("333 ==============");
                        } else if(qualificationStatus == -1){
                            check = false;
                            System.out.println("444 ==============");
                        }

                        if(check){
                            UpdateCandidateEducationProfileRequest.Builder educationBuilder = UpdateCandidateEducationProfileRequest.newBuilder();
                            educationBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                            educationBuilder.setCandidateEducationLevel(qualificationSelected);
                            if(qualificationSelected > 3){
                                educationBuilder.setCandidateDegree(degreeSelected);
                            }
                            educationBuilder.setCandidateEducationCompletionStatus(qualificationStatus);
                            educationBuilder.setCandidateEducationInstitute(candidateCollege.getText().toString());

                            UpdateEducationAsyncTask = new UpdateEducationProfileAsyncTask();
                            UpdateEducationAsyncTask.execute(educationBuilder.build());

                        }
                    }
                });
            }
        }
    }
}

