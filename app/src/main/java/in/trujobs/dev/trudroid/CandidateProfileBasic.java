package in.trujobs.dev.trudroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.GetCandidateBasicProfileStaticResponse;
import in.trujobs.proto.JobRoleObject;
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

    //UI References
    private EditText candidateDob, firstName, secondName, mobileNumber;
    Integer genderValue;
    Long shiftValue;

    private Button maleBtn, femaleBtn;
    private DatePickerDialog dobDatePicker;
    private SimpleDateFormat dateFormatter;
    public List<JobRoleObject> selectedJobRoles = new ArrayList<JobRoleObject>();

    public CandidateInfoActivity candidateInfoActivity;
    String jobRoleSelectedString = "";
    String jobRoleSelectedId = "";

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        candidateInfoActivity = (CandidateInfoActivity) getActivity();
        view = inflater.inflate(R.layout.candidate_basic_profile, container, false);

        mAsyncTask = new GetBasicStaticAsyncTask();
        mAsyncTask.execute();

        return view;
    }

    private void setDateTimeField() {
        candidateDob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dobDatePicker.show();
                return false;
            }
        });

        final Calendar newCalendar = Calendar.getInstance();

        dobDatePicker = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                candidateDob.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        dobDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
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
                Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
                ((CandidateInfoActivity)getActivity()).setSupportActionBar(toolbar);

                firstName = (EditText) view.findViewById(R.id.first_name_edit_text);
                secondName = (EditText) view.findViewById(R.id.last_name_edit_text);
                candidateDob = (EditText) view.findViewById(R.id.date_of_birth_edit_text);
                mobileNumber = (EditText) view.findViewById(R.id.phone_number);

                maleBtn = (Button) view.findViewById(R.id.gender_male);
                femaleBtn = (Button) view.findViewById(R.id.gender_female);

                maleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        genderValue = 0;
                        maleBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        maleBtn.setTextColor(getResources().getColor(R.color.white));
                        femaleBtn.setBackgroundColor(getResources().getColor(R.color.white));
                        femaleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                });

                femaleBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        genderValue = 1;
                        femaleBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        femaleBtn.setTextColor(getResources().getColor(R.color.white));
                        maleBtn.setBackgroundColor(getResources().getColor(R.color.white));
                        maleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                });

                dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                setDateTimeField();
                final Spinner shift_option = (Spinner) view.findViewById(R.id.shift_option);

                final String[] categories = new String[getCandidateBasicProfileStaticResponse.getTimeShiftListCount() + 1];
                final List<Integer> shiftIds = new ArrayList<Integer>();

                categories[0] = "Select Time Shift";
                shiftIds.add(-1);
                for(int i = 1; i<=getCandidateBasicProfileStaticResponse.getTimeShiftListCount();i++){
                    categories[i] = getCandidateBasicProfileStaticResponse.getTimeShiftListList().get(i-1).getTimeShiftName();
                    shiftIds.add((int) getCandidateBasicProfileStaticResponse.getTimeShiftListList().get(i-1).getTimeShiftId());
                }

                SpinnerAdapter adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, categories, 2);
                shift_option.setAdapter(adapter);

                final EditText jobPrefLocation = (EditText) view.findViewById(R.id.pref_job_roles);
                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateJobRolePrefCount() > 0){
                    selectedJobRoles.addAll(candidateInfoActivity.candidateInfo.getCandidate().getCandidateJobRolePrefList());
                    for(int i=0; i<selectedJobRoles.size(); i++ ){
                        jobRoleSelectedString = jobRoleSelectedString + selectedJobRoles.get(i).getJobRoleName();
                        jobRoleSelectedId = jobRoleSelectedId + selectedJobRoles.get(i).getJobRoleId();
                        if(i != (selectedJobRoles.size() - 1)){
                            jobRoleSelectedString += ", ";
                            jobRoleSelectedId += ",";
                        }
                    }
                    Prefs.jobPrefString.put(jobRoleSelectedId);
                    jobPrefLocation.setText(jobRoleSelectedString);
                }

                jobPrefLocation.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                            final CharSequence[] jobRoleList = new CharSequence[candidateInfoActivity.candidateInfo.getJobRolesCount()];
                            final List<Long> jobRoleIdList = new ArrayList<Long>();
                            for (int i = 0; i < candidateInfoActivity.candidateInfo.getJobRolesCount(); i++) {
                                jobRoleList[i] = candidateInfoActivity.candidateInfo.getJobRoles(i).getJobRoleName();
                                jobRoleIdList.add(candidateInfoActivity.candidateInfo.getJobRoles(i).getJobRoleId());
                            }

                            boolean[] checkedItems = new boolean[jobRoleList.length];
                            for(int i=0 ; i<jobRoleList.length ; i++){
                                for(JobRoleObject jobRoleObject: selectedJobRoles){
                                    if(jobRoleIdList.get(i) == jobRoleObject.getJobRoleId()){
                                        checkedItems[i] = true;
                                        break;
                                    } else{
                                        checkedItems[i] = false;
                                    }
                                }
                            }

                            final AlertDialog alertDialog = new AlertDialog.Builder(
                                    getContext())
                                    .setCancelable(false)
                                    .setTitle("Select Job Role preference (Max 3)")
                                    .setPositiveButton("Done",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    jobRoleSelectedString = "";
                                                    jobRoleSelectedId = "";
                                                    for(int i=0; i<selectedJobRoles.size(); i++ ){
                                                        jobRoleSelectedString = jobRoleSelectedString + selectedJobRoles.get(i).getJobRoleName();
                                                        jobRoleSelectedId = jobRoleSelectedId + selectedJobRoles.get(i).getJobRoleId();
                                                        if(i != (selectedJobRoles.size() - 1)){
                                                            jobRoleSelectedString += ", ";
                                                            jobRoleSelectedId += ",";
                                                        }
                                                    }
                                                    Prefs.jobPrefString.put(jobRoleSelectedId);
                                                    jobPrefLocation.setText(jobRoleSelectedString);
                                                    dialog.dismiss();
                                                }
                                            })
                                    .setMultiChoiceItems(jobRoleList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                            JobRoleObject.Builder currentJobRoleBuilder = JobRoleObject.newBuilder();
                                            if (b) {
                                                // If the user checked the item, add it to the selected items
                                                if(selectedJobRoles.size() < 3){
                                                    currentJobRoleBuilder.setJobRoleName(String.valueOf(jobRoleList[i]));
                                                    currentJobRoleBuilder.setJobRoleId(jobRoleIdList.get(i));
                                                    selectedJobRoles.add(currentJobRoleBuilder.build());
                                                    if(selectedJobRoles.size() == 3){
                                                       jobRoleSelectedString = "";
                                                       jobRoleSelectedId = "";
                                                        for(int x=0; x<selectedJobRoles.size(); x++ ){
                                                            jobRoleSelectedString = jobRoleSelectedString + selectedJobRoles.get(x).getJobRoleName();
                                                            jobRoleSelectedId = jobRoleSelectedId + selectedJobRoles.get(x).getJobRoleId();
                                                            if(x != (selectedJobRoles.size() - 1)){
                                                                jobRoleSelectedString += ", ";
                                                                jobRoleSelectedId += ",";
                                                            }
                                                        }
                                                        Prefs.jobPrefString.put(jobRoleSelectedId);
                                                        jobPrefLocation.setText(jobRoleSelectedString);
                                                        dialogInterface.dismiss();
                                                    }
                                                } else{
                                                    dialogInterface.dismiss();
                                                    Toast.makeText(getContext(), "Maximum 3 preference allowed.", Toast.LENGTH_LONG).show();
                                                }
                                            } else{
                                                for(int x=0; x<selectedJobRoles.size(); x++){
                                                    if(selectedJobRoles.get(x).getJobRoleId() == jobRoleIdList.get(i)){
                                                        selectedJobRoles.remove(x);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }).create();
                            alertDialog.show();
                            return true;
                        }
                        return false;
                    }
                });

                // prefilling data
                firstName.setText(candidateInfoActivity.candidateInfo.getCandidate().getCandidateFirstName());
                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateLastName() != null){
                    secondName.setText(candidateInfoActivity.candidateInfo.getCandidate().getCandidateLastName());
                }
                mobileNumber.setText(Prefs.candidateMobile.get());
                Toast.makeText(getContext(), "== " + candidateInfoActivity.candidateInfo.getCandidate().getCandidateDobMillis(),
                        Toast.LENGTH_LONG).show();
                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateDobMillis() != 0){
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(candidateInfoActivity.candidateInfo.getCandidate().getCandidateDobMillis());
                    int mYear = calendar.get(Calendar.YEAR);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    candidateDob.setText(mYear + "-" + (mMonth+1) + "-" + mDay);
                }

                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateGender() == 0){
                    genderValue = 0;
                    maleBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    maleBtn.setTextColor(getResources().getColor(R.color.white));
                    femaleBtn.setBackgroundColor(getResources().getColor(R.color.white));
                    femaleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else{
                    genderValue = 1;
                    femaleBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    femaleBtn.setTextColor(getResources().getColor(R.color.white));
                    maleBtn.setBackgroundColor(getResources().getColor(R.color.white));
                    maleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                if(candidateInfoActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref() != null){
                    int pos = -1;
                    for(int i=1 ; i<categories.length; i++){
                        if(shiftIds.get(i) == candidateInfoActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref().getTimeShiftId()){
                            pos = i;
                            break;
                        }
                    }
                    shiftValue = candidateInfoActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref().getTimeShiftId();
                    shift_option.setSelection(pos);
                } else{
                    shift_option.setSelection(0);
                }

                Button saveBasicProfileBtn = (Button) view.findViewById(R.id.button_save_basic);
                saveBasicProfileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean check = true;
                        int pos = shift_option.getSelectedItemPosition();
                        shiftValue = Long.valueOf(shiftIds.get(pos));
                        if(firstName.getText().toString() == null){
                            check = false;
                        } else if(candidateDob.getText().toString() == null){
                            check = false;
                        } else if(genderValue == null){
                            check = false;
                        } else if(shiftValue == null){
                            check = false;
                        } else if(selectedJobRoles.size() == 0){
                            check = false;
                        } else if(shift_option.getSelectedItemPosition() == 0){
                            check = false;
                        }

                        if(check){
                            UpdateCandidateBasicProfileRequest.Builder requestBuilder = UpdateCandidateBasicProfileRequest.newBuilder();
                            requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                            requestBuilder.setCandidateFirstName(firstName.getText().toString());
                            if(secondName.getText().toString() != null){
                                requestBuilder.setCandidateLastName(secondName.getText().toString());
                            }
                            requestBuilder.setCandidateDOB(candidateDob.getText().toString());
                            requestBuilder.setCandidateTimeshiftPref(shiftValue);
                            requestBuilder.setCandidateGender(genderValue);
                            requestBuilder.addAllJobRolePref(selectedJobRoles);
                            requestBuilder.setCandidateTimeshiftPref(shiftIds.get(shift_option.getSelectedItemPosition()));

                            mSaveBasicProfileAsyncTask = new UpdateBasicProfileAsyncTask();
                            mSaveBasicProfileAsyncTask.execute(requestBuilder.build());
                        }
                    }
                });
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
