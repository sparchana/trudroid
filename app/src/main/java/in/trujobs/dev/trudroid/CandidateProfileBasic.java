package in.trujobs.dev.trudroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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

import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.GetCandidateBasicProfileStaticResponse;
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

    public CandidateInfoActivity candidateInfoActivity;

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

                List<String> categories = new ArrayList<String>();
                final List<Integer> shiftIds = new ArrayList<Integer>();

                for(TimeShiftObject timeShiftObject : getCandidateBasicProfileStaticResponse.getTimeShiftListList()){
                    categories.add(timeShiftObject.getTimeShiftName());
                    shiftIds.add((int) timeShiftObject.getTimeShiftId());
                }

                ArrayAdapter<String> shift_adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, categories);

                shift_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                shift_option.setAdapter(shift_adapter);

/*                shift_option.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        int pos = shift_option.getSelectedItemPosition();
                        shiftValue = Long.valueOf(shiftIds.get(pos));
                    }
                });*/

                EditText jobPrefLocation = (EditText) view.findViewById(R.id.pref_location);
                jobPrefLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

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
                    candidateDob.setText(mDay + "-" + mMonth + "-" + mYear);

                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                    long milliSeconds = candidateInfoActivity.candidateInfo.getCandidate().getCandidateDobMillis();
                    System.out.println(milliSeconds + " 0-0-0-0-0-0");

                    Calendar calendarTest = Calendar.getInstance();
                    calendar.setTimeInMillis(milliSeconds);
                    System.out.println(formatter.format(calendar.getTime()) + " 0-0-0-0-0-0");

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
                    for(int i=0 ; i<categories.size(); i++){
                        if(categories.get(i) == candidateInfoActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref().getTimeShiftName()){
                            pos = i;
                            break;
                        }
                    }
                    shiftValue = candidateInfoActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref().getTimeShiftId();
                    shift_option.setSelection(pos);
                }


                Button saveBasicProfileBtn = (Button) view.findViewById(R.id.button_save_basic);
                saveBasicProfileBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean check = false;
                        int pos = shift_option.getSelectedItemPosition();
                        shiftValue = Long.valueOf(shiftIds.get(pos));
                        if(firstName.getText().toString() == null){
                            check = true;
                        } else if(candidateDob.getText().toString() == null){
                            check = true;
                        } else if(genderValue == null){
                            check = true;
                        } else if(shiftValue == null){
                            check = false;
                        }

                        if(check != true){
                            UpdateCandidateBasicProfileRequest.Builder requestBuilder = UpdateCandidateBasicProfileRequest.newBuilder();
                            requestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                            requestBuilder.setCandidateFirstName(firstName.getText().toString());
                            if(secondName.getText().toString() != null){
                                requestBuilder.setCandidateLastName(secondName.getText().toString());
                            }
                            requestBuilder.setCandidateDOB(candidateDob.getText().toString());
                            requestBuilder.setCandidateTimeshiftPref(shiftValue);
                            requestBuilder.setCandidateGender(genderValue);

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
