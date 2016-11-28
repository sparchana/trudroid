package in.trujobs.dev.trudroid.prescreen;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.R;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.GetCandidateEducationProfileStaticResponse;
import in.trujobs.proto.PreScreenEducationObject;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;
import in.trujobs.proto.UpdateCandidateEducationProfileRequest;

public class PreScreenEducation extends Fragment {
    ProgressDialog pd;
    private AsyncTask<UpdateCandidateEducationProfileRequest, Void, UpdateCandidateBasicProfileResponse> UpdateEducationAsyncTask;
    private AsyncTask<Void, Void, GetCandidateEducationProfileStaticResponse> mAsyncTask;

    LinearLayout degreeSection;
    LinearLayout qualificationLayout;
    LinearLayout degreeLayout;
    LinearLayout educationStatus;
    EditText candidateCollege;
    Button updateEducationProfile, educationStatusYes, educationStatusNo;
    int qualificationPos = 0, degreePos = 0, firstTimeSetting = 0;
    SpinnerAdapter adapter;
    public PreScreenActivity preScreenActivity;
    View view;

    String[] qualificationLevel = new String[0];
    Long[] qualificationId = new Long[0];

    String[] degreeName = new String[0];
    Long[] degreeId = new Long[0];

    private Long qualificationSelected = Long.valueOf(0), degreeSelected = Long.valueOf(0);
    Integer qualificationStatus = -1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        view = inflater.inflate(R.layout.pre_screen_education, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_EDUCATION_PRESCREEN);

        educationStatus = (LinearLayout) view.findViewById(R.id.education_status_layout);
        qualificationLayout = (LinearLayout) view.findViewById(R.id.qualification_layout);
        degreeLayout = (LinearLayout) view.findViewById(R.id.degree_layout);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Education Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        pd = CustomProgressDialog.get(getActivity());


        mAsyncTask = new GetEducationStaticAsyncTask();
        mAsyncTask.execute();

        PreScreenEducationObject preScreenEducationObject = null;
        Bundle bundle = getArguments();

        try {
            preScreenEducationObject = PreScreenEducationObject.parseFrom(bundle.getByteArray("education"));
            Tlog.i("education is matching: " + preScreenEducationObject.getIsMatching());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return view;
    }

    private class GetEducationStaticAsyncTask extends AsyncTask<Void,
            Void, GetCandidateEducationProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd = CustomProgressDialog.get(getActivity());
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
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if (getCandidateEducationProfileStaticResponse == null) {
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null Response");
                return;
            } else {
                if(getCandidateEducationProfileStaticResponse.getStatusValue() == ServerConstants.SUCCESS){
                    preScreenActivity = (PreScreenActivity) getActivity();

                    updateEducationProfile = (Button) view.findViewById(R.id.save_ps_education_btn);
                    final Spinner candidateQualification = (Spinner) view.findViewById(R.id.candidate_qualification);
                    final Spinner candidateDegree = (Spinner) view.findViewById(R.id.candidate_degree);

                    educationStatusYes = (Button) view.findViewById(R.id.education_status_yes);
                    educationStatusNo = (Button) view.findViewById(R.id.education_status_no);

                    qualificationLevel = new String[getCandidateEducationProfileStaticResponse.getEducationObjectCount() + 1];
                    qualificationId = new Long[getCandidateEducationProfileStaticResponse.getEducationObjectCount() + 1];

                    degreeName = new String[getCandidateEducationProfileStaticResponse.getDegreeObjectCount() + 1];
                    degreeId = new Long[getCandidateEducationProfileStaticResponse.getDegreeObjectCount() + 1];

                    qualificationLevel[0] = "Select Education Level";
                    qualificationId[0] = Long.valueOf(-1);
                    for(int i = 1; i<= getCandidateEducationProfileStaticResponse.getEducationObjectCount() ; i++){
                        qualificationLevel[i] = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationName();
                        qualificationId[i] = getCandidateEducationProfileStaticResponse.getEducationObject(i-1).getEducationId();
                    }
                    for(int i = 1; i<= getCandidateEducationProfileStaticResponse.getDegreeObjectCount() ; i++){
                        degreeName[i] = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeName();
                        degreeId[i] = getCandidateEducationProfileStaticResponse.getDegreeObject(i-1).getDegreeId();
                    }


                    degreeName[0] = "Select Highest Degree";
                    degreeId[0] = Long.valueOf(-1);

                    educationStatusYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            qualificationStatus = 1;
                            educationStatus.setBackgroundResource(0);
                            educationStatusYes.setBackgroundResource(R.drawable.rounded_corner_button);
                            educationStatusYes.setTextColor(getContext().getResources().getColor(R.color.white));
                            educationStatusNo.setBackgroundResource(R.drawable.round_white_button);
                            educationStatusNo.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                        }
                    });

                    educationStatusNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            qualificationStatus = 0;
                            educationStatus.setBackgroundResource(0);
                            educationStatusNo.setBackgroundResource(R.drawable.rounded_corner_button);
                            educationStatusNo.setTextColor(getContext().getResources().getColor(R.color.white));
                            educationStatusYes.setBackgroundResource(R.drawable.round_white_button);
                            educationStatusYes.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                        }
                    });

                    adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, qualificationLevel);
                    candidateQualification.setAdapter(adapter);
                    candidateQualification.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            qualificationLayout.setBackgroundResource(0);
                            return false;
                        }
                    });

                    candidateCollege = (EditText) view.findViewById(R.id.candidate_college);

                    degreeSection = (LinearLayout) view.findViewById(R.id.degree_section);
                    degreeSection.setVisibility(View.GONE);

                    adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, degreeName);
                    candidateDegree.setAdapter(adapter);

                    candidateDegree.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            degreeLayout.setBackgroundResource(0);
                            return false;
                        }
                    });

                    //pre-filling data
                    candidateQualification.setSelection(qualificationPos);
                    candidateDegree.setSelection(degreePos);

                    candidateQualification.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                        {
                            if(firstTimeSetting != 1){
                                qualificationStatus = -1;
                                educationStatusYes.setBackgroundResource(R.drawable.round_white_button);
                                educationStatusYes.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                                educationStatusNo.setBackgroundResource(R.drawable.round_white_button);
                                educationStatusNo.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                            } else{
                                firstTimeSetting = 0;
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
                            Tlog.e("Qualification status: = " + qualificationStatus);
                            if(qualificationSelected < 1){
                                showDialog("Please select your education level");
                                qualificationLayout.setBackgroundResource(R.drawable.border);
                                check = false;
                            } else if(qualificationSelected > 3 && degreeSelected < 1){
                                check = false;
                                showDialog("Please select your Degree");
                                degreeLayout.setBackgroundResource(R.drawable.border);
                            } else if(qualificationStatus == -1){
                                check = false;
                                showDialog("Please answer: \"Have you successfully completed this course?\"");
                                educationStatus.setBackgroundResource(R.drawable.border);
                            }

                            if(check){

                                //Track this action
                                ((PreScreenActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_EDIT_EDUCATION_PRESCREEN, Constants.GA_ACTION_SAVE_EDUCATION_PRESCREEN);

                                UpdateCandidateEducationProfileRequest.Builder educationBuilder = UpdateCandidateEducationProfileRequest.newBuilder();
                                educationBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                                educationBuilder.setCandidateEducationLevel(qualificationSelected);
                                if(qualificationSelected > 3){
                                    educationBuilder.setCandidateDegree(degreeSelected);
                                }
                                educationBuilder.setCandidateEducationCompletionStatus(qualificationStatus);
                                educationBuilder.setCandidateEducationInstitute(candidateCollege.getText().toString());

                                // this same api will add/update candidate education
                                UpdateEducationAsyncTask = new UpdateEducationProfileAsyncTask();
                                UpdateEducationAsyncTask.execute(educationBuilder.build());
                            }
                        }
                    });
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                            Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

    private class UpdateEducationProfileAsyncTask extends AsyncTask<UpdateCandidateEducationProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
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
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(updateCandidateBasicProfileResponse == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else{
                if(updateCandidateBasicProfileResponse.getStatusValue() == ServerConstants.SUCCESS){
                    PreScreenActivity.showRequiredFragment(getActivity());
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong while saving education profile. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void showDialog(String msg){
        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getContext()).create();
        alertDialog.setMessage(msg);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
