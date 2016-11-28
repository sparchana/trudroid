package in.trujobs.dev.trudroid.prescreen;


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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.protobuf.InvalidProtocolBufferException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.trujobs.dev.trudroid.Adapters.PlacesAutoCompleteAdapter;
import in.trujobs.dev.trudroid.Adapters.SpinnerAdapter;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicLatLngOrPlaceIdAsyncTask;
import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
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
import in.trujobs.proto.AssetObject;
import in.trujobs.proto.GenericResponse;
import in.trujobs.proto.GetCandidateBasicProfileStaticResponse;
import in.trujobs.proto.HomeLocalityRequest;
import in.trujobs.proto.HomeLocalityResponse;
import in.trujobs.proto.LatLngOrPlaceIdRequest;
import in.trujobs.proto.LocalityObjectResponse;
import in.trujobs.proto.PreScreenAssetObject;
import in.trujobs.proto.UpdateCandidateBasicProfileRequest;
import in.trujobs.proto.UpdateCandidateOtherRequest;

import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_ASSET_OWNED;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_GENDER;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_LOCALITY;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_MAX_AGE;
import static in.trujobs.dev.trudroid.Util.Constants.PROPERTY_TYPE_WORK_SHIFT;

public class PreScreenOthers extends Fragment {
    View view;
    private Toast mBaseToastLong;
    Spinner shift_option = null;
    private AsyncTask<HomeLocalityRequest, Void, HomeLocalityResponse> mUpdateLocatlityAsyncTask;
    private AsyncTask<Void, Void, GetCandidateBasicProfileStaticResponse> mAsyncTask;
    private AsyncTask<LatLngOrPlaceIdRequest, Void, LocalityObjectResponse> mLatLngOrPlaceIdAsyncTask;
    private static HomeLocalityRequest.Builder mHomeLocalityRequest = HomeLocalityRequest.newBuilder();

    private List<Integer> remainingPropIdList;
    private Button maleBtn, femaleBtn;
    private DatePickerDialog dobDatePicker;
    private EditText candidateDob;
    private Integer genderValue = -1;
    private Long shiftValue = Long.valueOf(-1);
    ProgressDialog pd;
    public android.support.design.widget.TextInputLayout shiftLayout;
    public LinearLayout genderBtnLayout;
    public LinearLayout assetListView;
    public LinearLayout workShiftView;
    List<Integer> shiftIds = new ArrayList<Integer>();

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

    // currently as per app flow, total exp resides within experience card
    // hence we won't show it here.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pre_screen_other_fragment, container, false);

        candidateDob = (EditText) view.findViewById(R.id.date_of_birth_edit_text);
        LinearLayout candidateDobLayout = (LinearLayout) view.findViewById(R.id.pre_screen_dob);

        mHomeLocalityTxtView = (AutoCompleteTextView) view.findViewById(R.id.home_locality_auto_complete_edit_text);
        LinearLayout homeLocalityLayout = (LinearLayout) view.findViewById(R.id.home_locality_layout);

        genderBtnLayout = (LinearLayout) view.findViewById(R.id.gender_button_layout);
        maleBtn = (Button) view.findViewById(R.id.gender_male);
        femaleBtn = (Button) view.findViewById(R.id.gender_female);

        assetListView = (LinearLayout) view.findViewById(R.id.asset_list_view);
        workShiftView = (LinearLayout) view.findViewById(R.id.pre_screen_time_shift_layout);
        PreScreenAssetObject preScreenAssetObject;
        Bundle bundle = getArguments();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

        ((PreScreenActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // track screen view
        ((PreScreenActivity) getActivity()).addScreenViewGA(Constants.GA_SCREEN_NAME_EDIT_OTHER_DETAIL_PRESCREEN);


        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setTitle("Important Details");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        pd = CustomProgressDialog.get(getActivity());

        remainingPropIdList = new ArrayList<>();
        for(Object integer : PreScreenActivity.propertyIdQueue) {
            remainingPropIdList.add((int) integer);
        }
        // all elements in this view are by default set to VISIBILITY.GONE
        // show only those which needed to be shown

        if(remainingPropIdList.contains(PROPERTY_TYPE_MAX_AGE)){
            candidateDobLayout.setVisibility(view.VISIBLE);
        }
        if(remainingPropIdList.contains(PROPERTY_TYPE_GENDER)){
            genderBtnLayout.setVisibility(view.VISIBLE);
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
        }
        if(remainingPropIdList.contains(PROPERTY_TYPE_LOCALITY)){
            homeLocalityLayout.setVisibility(view.VISIBLE);
        }
        if(remainingPropIdList.contains(PROPERTY_TYPE_WORK_SHIFT)){
            workShiftView.setVisibility(view.VISIBLE);
            mAsyncTask = new PreScreenOthers.GetBasicStaticAsyncTask();
            mAsyncTask.execute();
        }
        shiftLayout = (android.support.design.widget.TextInputLayout) view.findViewById(R.id.shift_layout);


        if(remainingPropIdList.contains(PROPERTY_TYPE_ASSET_OWNED)) {
            assetListView.setVisibility(view.VISIBLE);
            // render assets
            try {
                preScreenAssetObject = PreScreenAssetObject.parseFrom(bundle.getByteArray("asset"));
                for(AssetObject assetObject : preScreenAssetObject.getJobPostAssetList()){
                    View mLinearView = inflater.inflate(R.layout.pre_screen_asset_item, null);
                    TextView assetTitle = (TextView) mLinearView
                            .findViewById(R.id.asset_title);
                    assetTitle.setText(assetObject.getAssetTitle());
                    assetListView.addView(mLinearView);
                    final CheckBox assetCheckbox = (CheckBox) mLinearView.findViewById(R.id.asset_checkbox);
                    assetCheckbox.setId(assetObject.getAssetId());
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }

        Button saveBasicProfileBtn = (Button) view.findViewById(R.id.button_pre_screen_other);
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

                if(candidateDob.getText().toString().trim().isEmpty()){
                    check = false;
                    candidateDob.setError("Select date of birth");
                    candidateDob.addTextChangedListener(new PreScreenOthers.GenericTextWatcher(candidateDob));
                    showDialog("Please enter your Date of Birth");
                } else if(ageDiff < 0 || minAgeDiff > 0){
                    check = false;
                    candidateDob.setError("Select valid date of birth (min: 18 yrs, max: 80 yrs)");
                    candidateDob.addTextChangedListener(new PreScreenOthers.GenericTextWatcher(candidateDob));
                    showDialog("Please provide a valid date of birth (min: 18 yrs, max: 80 yrs  )");
                } else if(genderValue < 0){
                    check = false;
                    genderBtnLayout.setBackgroundResource(R.drawable.border);
                    showDialog("Please provide your gender");
                } else if(mHomeLocalityTxtView.getText().toString().length() == 0 ){
                    check = false;
                    mHomeLocalityTxtView.setError("Please provide your Home Locality");
                    mHomeLocalityTxtView.addTextChangedListener(new PreScreenOthers.GenericTextWatcher(mHomeLocalityTxtView));
                    showDialog("Please provide your Home Locality");
                } else if(shiftValue < 1 ){
                    check = false;
                    showDialog("Please provide your preferred Time Shift");
                    shiftLayout.setBackgroundResource(R.drawable.border);
                }

                if(check){
                    //Track this action
                    ((PreScreenActivity) getActivity()).addActionGA(Constants.GA_SCREEN_NAME_EDIT_OTHER_DETAIL_PRESCREEN, Constants.GA_ACTION_SAVE_OTHER_DETAIL_PRESCREEN);

                    //adding candidate's home locality
                    triggerFinalSubmission();

                    //update other basic information
                    UpdateCandidateBasicProfileRequest.Builder updateCandidateBasicProfileRequestBuilder = UpdateCandidateBasicProfileRequest.newBuilder();
                    updateCandidateBasicProfileRequestBuilder.setCandidateDOB(candidateDob.getText().toString());
                    updateCandidateBasicProfileRequestBuilder.setCandidateTimeshiftPref(shiftValue);
                    updateCandidateBasicProfileRequestBuilder.setCandidateGender(genderValue);
                    updateCandidateBasicProfileRequestBuilder.setCandidateTimeshiftPref(shiftIds.get(shift_option.getSelectedItemPosition()));


                    // update pre screen other value Async Task will come here
                }
            }
        });

        Tlog.i("remaining ids, that needed to be shown in one fragment: ");
        return view;
    }
    public void triggerFinalSubmission(){
        mHomeLocalityRequest.setCandidateMobile(Prefs.candidateMobile.get());
        mHomeLocalityRequest.setCandidateId(Prefs.candidateId.get());
        mHomeLocalityRequest.setLocalityName(mAddressOutput);

        mHomeLocalityRequest.setLat( mLastLocation.getLatitude());
        mHomeLocalityRequest.setLng( mLastLocation.getLongitude());
        if(mPlaceId!=null) mHomeLocalityRequest.setPlaceId( mPlaceId);

        mUpdateLocatlityAsyncTask = new PreScreenOthers.HomeLocalityAsyncTask();
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
                    ((PreScreenActivity)getActivity()).setSupportActionBar(toolbar);

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
                            mLatLngOrPlaceIdAsyncTask = new PreScreenOthers.LatLngOrPlaceIdAsyncTask();
                            mLatLngOrPlaceIdAsyncTask.execute(latLngOrPlaceIdRequest.build());
                        }
                    });


                    setDateTimeField();
                    shift_option = (Spinner) view.findViewById(R.id.shift_option);

                    final String[] categories = new String[getCandidateBasicProfileStaticResponse.getTimeShiftListCount() + 1];
                    shiftIds = new ArrayList<Integer>();

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


                    ImageView homeLocalityPicker = (ImageView) view.findViewById(R.id.home_locality_picker);

                    homeLocalityPicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mHomeLocalityTxtView.requestFocus();
                            mHomeLocalityTxtView.setSelection(mHomeLocalityTxtView.getText().length());
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(mHomeLocalityTxtView, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });

                    final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");


                    shift_option.setSelection(0);


                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                            Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        }
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


    private class UpdatePreScreenOtherAsyncTask extends AsyncTask<UpdateCandidateOtherRequest,
            Void, GenericResponse> {
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected GenericResponse doInBackground(UpdateCandidateOtherRequest ... params) {
            return HttpRequest.updateCandidateOther(params[0]);
        }

        @Override
        protected void onPostExecute(GenericResponse response) {
            super.onPostExecute(response);
            pd.cancel();
            if(!Util.isConnectedToInternet(getContext())) {
                Toast.makeText(getContext(), MessageConstants.NOT_CONNECTED, Toast.LENGTH_LONG).show();
            } else if(response == null){
                Toast.makeText(getContext(), "Looks like something went wrong. Please try again.",
                        Toast.LENGTH_LONG).show();
            } else {
                if(response.getStatus() == GenericResponse.Status.SUCCESS){
                    // todo call to go to interview selection comes here
                } else{
                    Toast.makeText(getContext(), "Looks like something went wrong while saving education profile. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
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

    private class GenericTextWatcher implements TextWatcher {

        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch(view.getId()){
                case R.id.date_of_birth_edit_text:
                    candidateDob.setError(null);
                    break;
                case R.id.home_locality_auto_complete_edit_text:
                    mHomeLocalityTxtView.setError(null);
                    break;
            }
        }
    }

}
