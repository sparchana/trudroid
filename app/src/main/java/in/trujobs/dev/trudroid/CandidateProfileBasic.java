package in.trujobs.dev.trudroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.trujobs.dev.trudroid.Adapters.PlacesAutoCompleteAdapter;
import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicLatLngOrPlaceIdAsyncTask;
import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.Util.Util;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.MessageConstants;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.GetCandidateBasicProfileStaticResponse;
import in.trujobs.proto.HomeLocalityRequest;
import in.trujobs.proto.HomeLocalityResponse;
import in.trujobs.proto.JobRoleObject;
import in.trujobs.proto.LatLngOrPlaceIdRequest;
import in.trujobs.proto.LocalityObjectResponse;
import in.trujobs.proto.UpdateCandidateBasicProfileRequest;
import in.trujobs.proto.UpdateCandidateBasicProfileResponse;

/**
 * Created by batcoder1 on 2/8/16.
 */
public class CandidateProfileBasic extends Fragment {
    private Toast mBaseToastLong;
    private AsyncTask<HomeLocalityRequest, Void, HomeLocalityResponse> mUpdateLocatlityAsyncTask;
    private AsyncTask<Void, Void, GetCandidateBasicProfileStaticResponse> mAsyncTask;
    private AsyncTask<UpdateCandidateBasicProfileRequest, Void, UpdateCandidateBasicProfileResponse> mSaveBasicProfileAsyncTask;
    ProgressDialog pd;
    private AutoCompleteTextView mHomeLocalityTxtView;

    /**
     * The formatted address.
     */
    public String mAddressOutput;

    /**
     * Represents a google's  place_id.
     */
    protected String mPlaceId;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected boolean GET_LOCALITY_FROM_GPS = false;
    protected boolean GET_LOCALITY_FROM_AUTOCOMPLETE = false;

    private AsyncTask<LatLngOrPlaceIdRequest, Void, LocalityObjectResponse> mLatLngOrPlaceIdAsyncTask;
    private static HomeLocalityRequest.Builder mHomeLocalityRequest = HomeLocalityRequest.newBuilder();

    //UI References
    private EditText candidateDob, firstName, secondName, mobileNumber, jobPrefEditText;
    Integer genderValue = -1;
    Long shiftValue = Long.valueOf(-1);

    private Button maleBtn, femaleBtn;
    private DatePickerDialog dobDatePicker;
    public List<JobRoleObject> selectedJobRoles = new ArrayList<JobRoleObject>();

    public CandidateProfileActivity candidateProfileActivity;
    String jobRoleSelectedString = "";
    String jobRoleSelectedId = "";

    public LinearLayout genderBtnLayout;

    public android.support.design.widget.TextInputLayout shiftLayout;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // track screen view
        ((CandidateProfileActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_BASIC_PROFILE);

        candidateProfileActivity = (CandidateProfileActivity) getActivity();
        view = inflater.inflate(R.layout.candidate_basic_profile, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((CandidateProfileActivity)getActivity()).setSupportActionBar(toolbar);

        ((CandidateProfileActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Basic Profile");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        shiftLayout = (android.support.design.widget.TextInputLayout) view.findViewById(R.id.shift_layout);

        pd = CustomProgressDialog.get(getActivity());
        mAsyncTask = new GetBasicStaticAsyncTask();
        mAsyncTask.execute();

        return view;
    }

    private void setDateTimeField(){
        candidateDob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dobDatePicker.show();
                return false;
            }
        });

        final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        //default date
        String dateStr = "01-01-1990";
        Date dateObj = new Date();
        try {
            dateObj = dateFormatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //setting 1-1-1990
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(dateObj);
        dobDatePicker = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                candidateDob.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -18);
        dobDatePicker.getDatePicker().setMaxDate(c.getTime().getTime());
    }

    private class GetBasicStaticAsyncTask extends AsyncTask<Void,
    Void, GetCandidateBasicProfileStaticResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
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
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if (getCandidateBasicProfileStaticResponse == null) {
                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                            Toast.LENGTH_LONG).show();
                    Log.w("", "Null candidate Response");
                return;
            } else {
                if(getCandidateBasicProfileStaticResponse.getStatusValue() == ServerConstants.SUCCESS){
                    Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
                    ((CandidateProfileActivity)getActivity()).setSupportActionBar(toolbar);

                    firstName = (EditText) view.findViewById(R.id.first_name_edit_text);
                    secondName = (EditText) view.findViewById(R.id.last_name_edit_text);
                    candidateDob = (EditText) view.findViewById(R.id.date_of_birth_edit_text);
                    mobileNumber = (EditText) view.findViewById(R.id.phone_number);
                    mHomeLocalityTxtView = (AutoCompleteTextView) view.findViewById(R.id.home_locality_auto_complete_edit_text);
                    genderBtnLayout = (LinearLayout) view.findViewById(R.id.gender_button_layout);

                    maleBtn = (Button) view.findViewById(R.id.gender_male);
                    femaleBtn = (Button) view.findViewById(R.id.gender_female);

                    mHomeLocalityTxtView.setAdapter(new PlacesAutoCompleteAdapter(getContext(), R.layout.place_autocomplete_list_item));
                    mHomeLocalityTxtView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // Get data associated with the specified position
                            // in the list (AdapterView)
                            //mAddressOutput = (String) parent.getItemAtPosition(position);
                            GET_LOCALITY_FROM_GPS = false;
                            GET_LOCALITY_FROM_AUTOCOMPLETE = true;
                            PlaceAPIHelper placeAPIHelper = (PlaceAPIHelper) parent.getItemAtPosition(position);
                            mAddressOutput = placeAPIHelper.getDescription();
                            mPlaceId = placeAPIHelper.getPlaceId();
                            Tlog.i("mAddressOutput ------ " + mAddressOutput
                                    + "\nplaceId:" + mPlaceId);

                            LatLngOrPlaceIdRequest.Builder latLngOrPlaceIdRequest = LatLngOrPlaceIdRequest.newBuilder();
                            if(!mPlaceId.trim().isEmpty()){
                                latLngOrPlaceIdRequest.setPlaceId(mPlaceId);
                            }
                            mLatLngOrPlaceIdAsyncTask = new LatLngOrPlaceIdAsyncTask();
                            mLatLngOrPlaceIdAsyncTask.execute(latLngOrPlaceIdRequest.build());
                        }
                    });
                    maleBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            genderValue = 0;
                            genderBtnLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                            maleBtn.setBackgroundResource(R.drawable.rounded_corner_button);
                            maleBtn.setTextColor(getResources().getColor(R.color.white));
                            femaleBtn.setBackgroundResource(R.drawable.round_white_button);
                            femaleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    });

                    femaleBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            genderValue = 1;
                            genderBtnLayout.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
                            femaleBtn.setBackgroundResource(R.drawable.rounded_corner_button);
                            femaleBtn.setTextColor(getResources().getColor(R.color.white));
                            maleBtn.setBackgroundResource(R.drawable.round_white_button);
                            maleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                    });

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

                    SpinnerAdapter adapter = new SpinnerAdapter(getContext(), R.layout.spinner_layout, categories);
                    shift_option.setAdapter(adapter);

                    shift_option.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            shiftLayout.setBackgroundResource(0);
                            return false;
                        }
                    });

                    jobPrefEditText = (EditText) view.findViewById(R.id.pref_job_roles);
                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateJobRolePrefCount() > 0){
                        selectedJobRoles.addAll(candidateProfileActivity.candidateInfo.getCandidate().getCandidateJobRolePrefList());
                        for(int i=0; i<selectedJobRoles.size(); i++ ){
                            jobRoleSelectedString = jobRoleSelectedString + selectedJobRoles.get(i).getJobRoleName();
                            jobRoleSelectedId = jobRoleSelectedId + selectedJobRoles.get(i).getJobRoleId();
                            if(i != (selectedJobRoles.size() - 1)){
                                jobRoleSelectedString += ", ";
                                jobRoleSelectedId += ",";
                            }
                        }
                        Prefs.jobPrefString.put(jobRoleSelectedId);
                        jobPrefEditText.setText(jobRoleSelectedString);
                    }
                    final CharSequence[] jobRoleList = new CharSequence[candidateProfileActivity.candidateInfo.getJobRolesCount()];
                    final List<Long> jobRoleIdList = new ArrayList<Long>();
                    for (int i = 0; i < candidateProfileActivity.candidateInfo.getJobRolesCount(); i++) {
                        jobRoleList[i] = candidateProfileActivity.candidateInfo.getJobRoles(i).getJobRoleName();
                        jobRoleIdList.add(candidateProfileActivity.candidateInfo.getJobRoles(i).getJobRoleId());
                    }

                    final boolean[] checkedItems = new boolean[jobRoleList.length];
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
                    jobPrefEditText.setEnabled(false);
                    ImageView jobRolePrefPicker = (ImageView) view.findViewById(R.id.job_role_pref_picker);
                    ImageView homeLocalityPicker = (ImageView) view.findViewById(R.id.home_locality_picker);

                    jobRolePrefPicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showJobRolePrefPopup(jobRoleList, jobRoleIdList, checkedItems);
                        }
                    });

                    homeLocalityPicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mHomeLocalityTxtView.requestFocus();
                            mHomeLocalityTxtView.setSelection(mHomeLocalityTxtView.getText().length());
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mHomeLocalityTxtView, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });

                    // prefilling data
                    firstName.setText(candidateProfileActivity.candidateInfo.getCandidate().getCandidateFirstName());
                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateLastName() != null){
                        secondName.setText(candidateProfileActivity.candidateInfo.getCandidate().getCandidateLastName());
                    }
                    if(candidateProfileActivity.candidateInfo.getCandidate().hasCandidateHomelocality()){
                        mLastLocation = new Location("");
                        mAddressOutput = candidateProfileActivity.candidateInfo.getCandidate().getCandidateHomelocality().getLocalityName();
                        mLastLocation.setLatitude(candidateProfileActivity.candidateInfo.getCandidate().getCandidateHomelocality().getLat());
                        mLastLocation.setLongitude(candidateProfileActivity.candidateInfo.getCandidate().getCandidateHomelocality().getLng());
                        mHomeLocalityTxtView.setText(mAddressOutput);
                    }
                    mobileNumber.setText(Prefs.candidateMobile.get());
                    mobileNumber.setEnabled(false);
                    final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateDobMillis() != 0){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(candidateProfileActivity.candidateInfo.getCandidate().getCandidateDobMillis());
                        int mYear = calendar.get(Calendar.YEAR);
                        int mMonth = calendar.get(Calendar.MONTH) ;
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                        String cDay = mDay + "";
                        String cMonth = (mMonth + 1) + "";

                        if(mDay < 10){
                            cDay = "0" + mDay;
                        }
                        if((mMonth + 1) < 10){
                            cMonth = "0" + (mMonth + 1);
                        }

                        candidateDob.setText(cDay + "-" + cMonth + "-" + mYear);
                        dobDatePicker = new DatePickerDialog(getContext(), AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, monthOfYear, dayOfMonth);

                                candidateDob.setText(formatter.format(newDate.getTime()));
                            }
                        },mYear, mMonth, mDay);
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.YEAR, -18);
                        dobDatePicker.getDatePicker().setMaxDate(c.getTime().getTime());
                    }

                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateGender() == 0){
                        genderValue = 0;
                        maleBtn.setBackgroundResource(R.drawable.rounded_corner_button);
                        maleBtn.setTextColor(getResources().getColor(R.color.white));
                        femaleBtn.setBackgroundResource(R.drawable.round_white_button);
                        femaleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                    } else if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateGender() == 1){
                        genderValue = 1;
                        femaleBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        femaleBtn.setTextColor(getResources().getColor(R.color.white));
                        maleBtn.setBackgroundColor(getResources().getColor(R.color.white));
                        maleBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    if(candidateProfileActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref() != null){
                        int pos = -1;
                        for(int i=1 ; i<categories.length; i++){
                            if(shiftIds.get(i) == candidateProfileActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref().getTimeShiftId()){
                                pos = i;
                                break;
                            }
                        }
                        shiftValue = candidateProfileActivity.candidateInfo.getCandidate().getCandidateTimeShiftPref().getTimeShiftId();
                        shift_option.setSelection(pos);
                    } else{
                        shift_option.setSelection(0);
                    }

                    Button saveBasicProfileBtn = (Button) view.findViewById(R.id.button_save_basic);
                    saveBasicProfileBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            boolean check = true;

                            int year, ageDiff = 0, minAgeDiff = 0;
                            if(candidateDob.getText().toString().length() > 0){
                                String str[] = candidateDob.getText().toString().split("-");
                                year = Integer.parseInt(str[2]);
                                ageDiff = ((Calendar.getInstance().get(Calendar.YEAR) - 18) - year);
                                minAgeDiff = ((Calendar.getInstance().get(Calendar.YEAR) - 80) - year);
                            }

                            int pos = shift_option.getSelectedItemPosition();
                            shiftValue = Long.valueOf(shiftIds.get(pos));

                            if(Util.isValidName(firstName.getText().toString()) == 0){
                                showDialog("First Name Cannot be blank. Please enter your first name");
                                firstName.setError("Enter your first name");
                                firstName.addTextChangedListener(new GenericTextWatcher(firstName));
                                check = false;
                            } else if(Util.isValidName(firstName.getText().toString()) == 1) {
                                showDialog("First Name cannot have special characters or numbers. Please enter a valid first name");
                                firstName.setError("Enter a valid second name");
                                firstName.addTextChangedListener(new GenericTextWatcher(firstName));
                                check = false;
                            } else if(!secondName.getText().toString().isEmpty() && (Util.isValidName(secondName.getText().toString()) == 1)) {
                                showDialog("Second Name cannot have special characters or numbers. Please enter a valid second name");
                                secondName.setError("Enter your second name");
                                secondName.addTextChangedListener(new GenericTextWatcher(secondName));
                                check = false;
                            } else if(!secondName.getText().toString().isEmpty() && (Util.isValidName(secondName.getText().toString()) == 0)) {
                                showDialog("Second Name Cannot be blank. Please enter your second name");
                                secondName.setError("Enter valid second name");
                                secondName.addTextChangedListener(new GenericTextWatcher(secondName));
                                check = false;
                            } else if(candidateDob.getText().toString().trim().isEmpty()){
                                check = false;
                                candidateDob.setError("Select date of birth");
                                candidateDob.addTextChangedListener(new GenericTextWatcher(candidateDob));
                                showDialog("Please enter your Date of Birth");
                            } else if(ageDiff < 0 || minAgeDiff > 0){
                                check = false;
                                candidateDob.setError("Select valid date of birth (min: 18 yrs, max: 80 yrs)");
                                candidateDob.addTextChangedListener(new GenericTextWatcher(candidateDob));
                                showDialog("Please provide a valid date of birth (min: 18 yrs, max: 80 yrs  )");
                            } else if(genderValue < 0){
                                check = false;
                                genderBtnLayout.setBackgroundResource(R.drawable.border);
                                showDialog("Please provide your gender");
                            } else if(mHomeLocalityTxtView.getText().toString().length() == 0 ){
                                check = false;
                                mHomeLocalityTxtView.setError("Please provide your Home Locality");
                                mHomeLocalityTxtView.addTextChangedListener(new GenericTextWatcher(mHomeLocalityTxtView));
                                showDialog("Please provide your Home Locality");
                            } else if(selectedJobRoles.size() == 0){
                                check = false;
                                showDialog("Please provide your preferred Job Roles");
                            }  else if(shiftValue < 1 ){
                                check = false;
                                showDialog("Please provide your preferred Time Shift");
                                shiftLayout.setBackgroundResource(R.drawable.border);
                            }

                            if(check){
                                //Track this action
                                ((CandidateProfileActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_EDIT_BASIC_PROFILE, Constants.GA_ACTION_SAVE_BASIC_PROFILE);

                                //adding candidate's home locality
                                triggerFinalSubmission();

                                //update other basic information
                                UpdateCandidateBasicProfileRequest.Builder updateCandidateBasicProfileRequestBuilder = UpdateCandidateBasicProfileRequest.newBuilder();
                                updateCandidateBasicProfileRequestBuilder.setCandidateMobile(Prefs.candidateMobile.get());
                                updateCandidateBasicProfileRequestBuilder.setCandidateFirstName(firstName.getText().toString());
                                if(!secondName.getText().toString().isEmpty()){
                                    updateCandidateBasicProfileRequestBuilder.setCandidateLastName(secondName.getText().toString());
                                }
                                updateCandidateBasicProfileRequestBuilder.setCandidateDOB(candidateDob.getText().toString());
                                updateCandidateBasicProfileRequestBuilder.setCandidateTimeshiftPref(shiftValue);
                                updateCandidateBasicProfileRequestBuilder.setCandidateGender(genderValue);
                                updateCandidateBasicProfileRequestBuilder.addAllJobRolePref(selectedJobRoles);
                                updateCandidateBasicProfileRequestBuilder.setCandidateTimeshiftPref(shiftIds.get(shift_option.getSelectedItemPosition()));

                                mSaveBasicProfileAsyncTask = new UpdateBasicProfileAsyncTask();
                                mSaveBasicProfileAsyncTask.execute(updateCandidateBasicProfileRequestBuilder.build());
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

    private class GenericTextWatcher implements TextWatcher {

        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch(view.getId()){
                case R.id.first_name_edit_text:
                    firstName.setError(null);
                    break;
                case R.id.last_name_edit_text:
                    firstName.setError(null);
                    break;
                case R.id.date_of_birth_edit_text:
                    candidateDob.setError(null);
                    break;
                case R.id.home_locality_auto_complete_edit_text:
                    mHomeLocalityTxtView.setError(null);
                    break;
                case R.id.pref_job_roles:
                    jobPrefEditText.setError(null);
                    break;
            }
        }
    }

    public void triggerFinalSubmission(){
        mHomeLocalityRequest.setCandidateMobile(Prefs.candidateMobile.get());
        mHomeLocalityRequest.setCandidateId(Prefs.candidateId.get());
        mHomeLocalityRequest.setLocalityName(mAddressOutput);

        mHomeLocalityRequest.setLat( mLastLocation.getLatitude());
        mHomeLocalityRequest.setLng( mLastLocation.getLongitude());
        if(mPlaceId!=null) mHomeLocalityRequest.setPlaceId( mPlaceId);

        mUpdateLocatlityAsyncTask = new HomeLocalityAsyncTask();
        mUpdateLocatlityAsyncTask.execute(mHomeLocalityRequest.build());

            /* update prefs values */
        Prefs.candidateHomeLat.put(String.valueOf(mLastLocation.getLatitude()));
        Prefs.candidateHomeLng.put(String.valueOf(mLastLocation.getLongitude()));
    }

    private class HomeLocalityAsyncTask extends AsyncTask<HomeLocalityRequest,
            Void, HomeLocalityResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
                pd.show();
            }

        @Override
        protected HomeLocalityResponse doInBackground(HomeLocalityRequest... params) {
            return HttpRequest.addHomeLocality(params[0]);
        }

        @Override
        protected void onPostExecute(HomeLocalityResponse homeLocalityResponse) {
            super.onPostExecute(homeLocalityResponse);
            mAsyncTask = null;
            if (homeLocalityResponse == null) {
                Toast.makeText(getContext(), "Failed to set Home PlaceAPIHelper. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null Response");
                return;
            } else if (homeLocalityResponse.getStatusValue() == ServerConstants.SUCCESS){
                Prefs.candidateHomeLocalityStatus.put(ServerConstants.HOMELOCALITY_YES);
                Tlog.e("SUCCESS!");
            }
            else {
                Tlog.e("FAILED");
            }
        }
    }

    private class LatLngOrPlaceIdAsyncTask extends BasicLatLngOrPlaceIdAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
            Tlog.i("Fetching Locality Object from latlng....");
        }

        @Override
        protected void onPostExecute(LocalityObjectResponse localityObjectResponse) {
            super.onPostExecute(localityObjectResponse);
            pd.cancel();
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            if(localityObjectResponse!=null){
                if(localityObjectResponse.getStatus()== LocalityObjectResponse.Status.SUCCESS) {
                    switch (localityObjectResponse.getType()) {
                        case  FOR_PLACEID:
                            if(mLastLocation == null) {
                                mLastLocation = new Location("");
                            }
                            if(localityObjectResponse.getLocality().getLat()!=0){
                                mLastLocation.setLatitude(localityObjectResponse.getLocality().getLat());
                            }
                            if(localityObjectResponse.getLocality().getLng()!=0){
                                mLastLocation.setLongitude(localityObjectResponse.getLocality().getLng());
                            }
                            break;
                        case FOR_LATLNG:
                            /* since the req was made with latlng i.e the users latlng
                             and not the locality latlng, use that to make the final req*/
                            break;
                        default:
                            break;
                    }
                    /* common setters  */
                    if(!localityObjectResponse.getLocality().getLocalityName().isEmpty()){
                        mAddressOutput = localityObjectResponse.getLocality().getLocalityName();
                    }
                    if(!localityObjectResponse.getLocality().getPlaceId().trim().isEmpty()){
                        mPlaceId = localityObjectResponse.getLocality().getPlaceId();
                    }
                    mLatLngOrPlaceIdAsyncTask = null;
                    mHomeLocalityTxtView.setText(mAddressOutput);
                    mHomeLocalityTxtView.dismissDropDown();
                    mHomeLocalityTxtView.clearFocus();
                } else {
                    showToast("Error While Fetching Locality. Please manually type your locality above.");
                    mHomeLocalityTxtView.setText("");
                    mAddressOutput = "";
                    mHomeLocalityTxtView.clearFocus();
                }
            }
        }
    }



    private class UpdateBasicProfileAsyncTask extends AsyncTask<UpdateCandidateBasicProfileRequest,
            Void, UpdateCandidateBasicProfileResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected UpdateCandidateBasicProfileResponse doInBackground(UpdateCandidateBasicProfileRequest... params) {
            return HttpRequest.updateCandidateBasicProfile(params[0]);
        }

        @Override
        protected void onPostExecute(UpdateCandidateBasicProfileResponse updateCandidateBasicProfileResponse) {
            super.onPostExecute(updateCandidateBasicProfileResponse);
            pd.cancel();

            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if (updateCandidateBasicProfileResponse == null) {
                Toast.makeText(getContext(), "Something went Wrong. Please try again!",
                        Toast.LENGTH_LONG).show();
                Log.w("", "Null update Response");
                return;
            } else {
                if(updateCandidateBasicProfileResponse.getStatusValue() == ServerConstants.SUCCESS){
                    CandidateProfileExperience candidateProfileExperience = new CandidateProfileExperience();
                    candidateProfileExperience.setArguments(getActivity().getIntent().getExtras());
                    getFragmentManager().beginTransaction()
                            .addToBackStack(null)
                            .setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down)
                            .add(R.id.main_profile, candidateProfileExperience).commit();
                    updatePrefs();
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong while saving basic profile. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void updatePrefs() {
        if(selectedJobRoles.size()>0){
            Prefs.candidatePrefJobRoleIdOne.remove();
            Prefs.candidatePrefJobRoleIdTwo.remove();
            Prefs.candidatePrefJobRoleIdThree.remove();
            if(selectedJobRoles.size()>0 && selectedJobRoles.get(0).getJobRoleId() != 0) {
                Prefs.candidatePrefJobRoleIdOne.put(selectedJobRoles.get(0).getJobRoleId());
                Prefs.candidateJobPrefStatus.put(ServerConstants.JOBPREFERENCE_YES);
            }
            if(selectedJobRoles.size()>1 && selectedJobRoles.get(1).getJobRoleId() != 0) Prefs.candidatePrefJobRoleIdTwo.put(selectedJobRoles.get(1).getJobRoleId());
            if(selectedJobRoles.size()>2 && selectedJobRoles.get(2).getJobRoleId() != 0) Prefs.candidatePrefJobRoleIdThree.put(selectedJobRoles.get(2).getJobRoleId());
            Prefs.candidateHomeLocalityName.put(mAddressOutput);
            Prefs.candidateHomeLat.put(String.valueOf(mLastLocation.getLatitude()));
            Prefs.candidateHomeLng.put(String.valueOf(mLastLocation.getLongitude()));
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

    private void showJobRolePrefPopup(final CharSequence[] jobRoleNameList, final List<Long> jobRoleIdList, final boolean[] checkedItems) {

        final android.support.v7.app.AlertDialog.Builder searchByJobRoleBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());
        searchByJobRoleBuilder.setCancelable(false);
        searchByJobRoleBuilder.setTitle("Select Job Role preference (Max 3)");
        searchByJobRoleBuilder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey (DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK &&
                        event.getAction() == KeyEvent.ACTION_UP &&
                        !event.isCanceled()) {
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });
        searchByJobRoleBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
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
                jobPrefEditText.setText(jobRoleSelectedString);
                dialog.cancel();
            }
        });
        searchByJobRoleBuilder.setNeutralButton("Clear All", null);
        searchByJobRoleBuilder.setMultiChoiceItems(jobRoleNameList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                JobRoleObject.Builder currentJobRoleBuilder = JobRoleObject.newBuilder();
                if(isChecked) {
                    if (selectedJobRoles.size() < 3) {
                        currentJobRoleBuilder.setJobRoleName(String.valueOf(jobRoleNameList[which]));
                        currentJobRoleBuilder.setJobRoleId(jobRoleIdList.get(which));
                        selectedJobRoles.add(currentJobRoleBuilder.build());
                    } else {
                        checkedItems[which] = false;
                        ((android.support.v7.app.AlertDialog) dialogInterface).getListView().setItemChecked(which, false);
                        Toast.makeText(getContext(), "Please select only maximum of 3 job roles.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    for(int i=0; i< selectedJobRoles.size(); i++){
                        if(selectedJobRoles.get(i).getJobRoleId() == jobRoleIdList.get(which)){
                            selectedJobRoles.remove(i);
                            break;
                        }
                    }
                    checkedItems[which] = false;
                }
            }
        });
        final android.support.v7.app.AlertDialog searchByJobRoleDialog = searchByJobRoleBuilder.create();
        searchByJobRoleDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button b = searchByJobRoleDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEUTRAL);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedJobRoles.clear();
                        Arrays.fill(checkedItems, false);
                        jobPrefEditText.setText("");
                        showToast("Selection Cleared.");
                        for(int which=0; which<checkedItems.length; which++){
                            ((android.support.v7.app.AlertDialog) dialog).getListView().setItemChecked(which, false);
                        }
                        //Dismiss once everything is OK.
                    }
                });
            }
        });

        searchByJobRoleDialog.show();
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String msg) {
        try{ mBaseToastLong.getView().isShown();     // true if visible
            mBaseToastLong.setText(msg);
        } catch (Exception e) {         // invisible if exception
            mBaseToastLong = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        }
        mBaseToastLong.show();
    }

}