package in.trujobs.dev.trudroid;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import in.trujobs.dev.trudroid.Adapters.PlacesAutoCompleteAdapter;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicLatLngOrPlaceIdAsyncTask;
import in.trujobs.dev.trudroid.Helper.LatLngAPIHelper;
import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.CustomProgressDialog;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.HomeLocalityRequest;
import in.trujobs.proto.HomeLocalityResponse;
import in.trujobs.proto.LatLngOrPlaceIdRequest;
import in.trujobs.proto.LocalityObjectResponse;

public class HomeLocality extends TruJobsBaseActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = "HomeLocationActivity";

    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    protected boolean GET_LOCALITY_FROM_GPS = false;
    protected boolean GET_LOCALITY_FROM_AUTOCOMPLETE = false;

    public Toast mToast;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    /**
     * set to true if GPS is enable in phone.
     */
    protected boolean mLocationEnabled;
    protected boolean showProgressBar;

    /**
     * Represents a google's  place_id.
     */
    protected String mPlaceId;

    /**
     * Tracks whether the user has requested an address. Becomes true when the user requests an
     * address and false when the address (or an error message) is delivered.
     * The user requests an address by pressing the Fetch Address button. This may happen
     * before GoogleApiClient connects. This activity uses this boolean to keep track of the
     * user's intent. If the value is true, the activity tries to fetch the address as soon as
     * GoogleApiClient connects.
     */
    protected boolean mAddressRequested;

    /**
     * The formatted address.
     */
    public String mAddressOutput;
    /**
     * Visible while the address is being fetched.
     */
    protected ProgressBar mProgressBar;
    /**
     * Kicks off the request to fetch an address when pressed.
     */
    protected Button mFetchAddressButton;

    protected Button mSaveHomeLocality;
    /**
     * Displays the AutoComplete Selected PlaceAPIHelper.
     */
    protected AutoCompleteTextView mSearchHomeLocalityTxtView;

    private static final int REQUEST_CHECK_SETTINGS = 2;

    private static HomeLocalityRequest.Builder mHomeLocalityRequest = HomeLocalityRequest.newBuilder();


    private AsyncTask<HomeLocalityRequest, Void, HomeLocalityResponse> mAsyncTask;
    private AsyncTask<String, Void, LatLngAPIHelper> mLatLngAsyncTask;

    public ImageView clearAutoCompleteBtn;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tlog.wtf("Home Locality Activity created");
        setContentView(R.layout.activity_home_locality);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Hi " + Prefs.firstName.get() + "!");

        pd = CustomProgressDialog.get(HomeLocality.this);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mFetchAddressButton = (Button) findViewById(R.id.current_loc);
        mSaveHomeLocality = (Button) findViewById(R.id.saveHomeLocality);
        activateOrDeactivateSubmitButton(false);

        // Set defaults, then update using values stored in the Bundle.
        mAddressRequested = false;
        mAddressOutput = "";
        updateValuesFromBundle(savedInstanceState);
        mSearchHomeLocalityTxtView = (AutoCompleteTextView) findViewById(R.id.search_home_locality_autocomplete);
        mSearchHomeLocalityTxtView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.place_autocomplete_list_item));
        mSearchHomeLocalityTxtView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                //mAddressOutput = (String) parent.getItemAtPosition(position);
                GET_LOCALITY_FROM_GPS = false;
                GET_LOCALITY_FROM_AUTOCOMPLETE = true;
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
                PlaceAPIHelper placeAPIHelper = (PlaceAPIHelper) parent.getItemAtPosition(position);
                mPlaceId = placeAPIHelper.getPlaceId();
                mAddressOutput = placeAPIHelper.getDescription();
                Tlog.i("mAddressOutput ------ " + mAddressOutput
                        + "\nplaceId:" + mPlaceId);
                triggerPlaceIdToLocalityResolver(mPlaceId);
                showProgressBar = false;
                updateUIWidgets();
            }
        });

        clearAutoCompleteBtn= (ImageView) findViewById(R.id.home_locality_auto_complete_clear);
        clearAutoCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchHomeLocalityTxtView.getText().clear();
                mSearchHomeLocalityTxtView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mSearchHomeLocalityTxtView, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        mSaveHomeLocality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveHomeLocality(view);
            }
        });
        mFetchAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchAddressButtonHandler(view);
            }
        });
        updateUIWidgets();
        buildGoogleApiClient();

        // Open the autocomplete activity when the button is clicked.
    }

    public void activateOrDeactivateSubmitButton(boolean shouldActivate) {
        if (shouldActivate) {
            mSaveHomeLocality.setEnabled(true);
            mSaveHomeLocality.setBackgroundColor(Color.parseColor("#2d77ba"));
            mSaveHomeLocality.setBackgroundResource(R.color.colorPrimary);
        } else {
            mSaveHomeLocality.setEnabled(false);
            //mSaveHomeLocality.setBackgroundColor(Color.GRAY);
            mSaveHomeLocality.setBackgroundResource(R.color.back_grey_dark);
        }
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                displayAddressOutput();
            }
        }
    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        } else {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Runs when user clicks the Use Current Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    public void fetchAddressButtonHandler(View view) {
        Tlog.i("user current location triggered..");
        showProgressBar = true;
        updateUIWidgets();
        satisfyLocationSettings();
        /*final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }*/
    }

    private void satisfyLocationSettings() {
        /* Before all check if network is available */

        buildGoogleApiClient();
        if (!CheckNetworkStatus()) {
            showProgressBar = false;
            updateUIWidgets();
            return;
        }
        GET_LOCALITY_FROM_GPS = true;
        GET_LOCALITY_FROM_AUTOCOMPLETE = false;
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationRequest mLocationRequestBalancedPowerAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy)
                .addLocationRequest(mLocationRequestBalancedPowerAccuracy);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        // We only start the service to fetch the address if GoogleApiClient is connected.
                        mAddressRequested = true;
                        fetchCurrentAddress();
                        mAddressRequested = false;
                        // If GoogleApiClient isn't connected, we process the user's request by setting
                        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
                        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
                        // immediately kicks off the process of getting the address.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    HomeLocality.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        mAddressRequested = true;
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        showToast("Something went wrong with gps setting of your device. Please type" +
                                "your locality ");
                        mAddressRequested = false;
                        break;
                }
                updateUIWidgets();
            }
        });
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        mAddressRequested = false;
                        updateUIWidgets();
                        showToast("GPS not enabled. Please select your locality.");
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        triggerGPS();
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void triggerLatLngToLocalityResolver() {
        LatLngOrPlaceIdRequest.Builder latLngOrPlaceIdRequest = LatLngOrPlaceIdRequest.newBuilder();
        if(mLastLocation!=null){
            latLngOrPlaceIdRequest.setLatitude(mLastLocation.getLatitude());
            latLngOrPlaceIdRequest.setLongitude(mLastLocation.getLongitude());
        } else {
            mSearchHomeLocalityTxtView.setText("");
            showToast("Unable to detect location. Please manually type the location");
        }
        LatLngOrPlaceIdAsyncTask localityFromLatLngOrPlaceIdAsyncTask = new LatLngOrPlaceIdAsyncTask();
        localityFromLatLngOrPlaceIdAsyncTask.execute(latLngOrPlaceIdRequest.build());
    }
    protected void triggerPlaceIdToLocalityResolver(String placeId) {
        LatLngOrPlaceIdRequest.Builder latLngOrPlaceIdRequest = LatLngOrPlaceIdRequest.newBuilder();
        if(!placeId.trim().isEmpty()){
            latLngOrPlaceIdRequest.setPlaceId(placeId);
        }
        LatLngOrPlaceIdAsyncTask localityFromLatLngOrPlaceIdAsyncTask = new LatLngOrPlaceIdAsyncTask();
        localityFromLatLngOrPlaceIdAsyncTask.execute(latLngOrPlaceIdRequest.build());
    }

    private void fetchCurrentAddress() {
        triggerGPS();

        if (mGoogleApiClient.isConnected() && mLastLocation != null && mLocationEnabled) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                return;
            }
            // It is possible that the user presses the button to get the address before the
            // GoogleApiClient object successfully connects. In such a case, mAddressRequested
            // is set to true, but no attempt is made to fetch the address (see
            // fetchAddressButtonHandler()) . Instead, we start the intent service here if the
            // user has requested an address, since we now have a connection to GoogleApiClient.
            if (mAddressRequested) {
                showProgressBar = true;
                updateUIWidgets();
                triggerLatLngToLocalityResolver();
            }
        }
    }

    private void triggerGPS() {
        // Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            showToast("Location Permission Not Granted. Please Enter Your Home Location.");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        /* enable gps */
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Tlog.w("gps not enabled...popped up question");
            buildAlertMessageNoGps();
            mLocationEnabled = false;
        } else {
            mLocationEnabled = true;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        triggerGPS();
        if (result.getErrorCode() == 2) {
            showProgressBar = false;
            updateUIWidgets();
            satisfyLocationSettings();
            showToast("Please Update Google Services");
        }
        Tlog.i( "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Tlog.i( "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Updates the address in the UI.
     */
    protected void displayAddressOutput() {
        GET_LOCALITY_FROM_AUTOCOMPLETE = false;
        GET_LOCALITY_FROM_GPS = true;
        mSearchHomeLocalityTxtView.setText(mAddressOutput);
        mSearchHomeLocalityTxtView.dismissDropDown();
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private void updateUIWidgets() {
        if (showProgressBar) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mFetchAddressButton.setEnabled(false);
            activateOrDeactivateSubmitButton(false);
        } else if (mAddressRequested) {
            mProgressBar.setVisibility(ProgressBar.VISIBLE);
            mFetchAddressButton.setEnabled(false);
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
            mFetchAddressButton.setEnabled(true);
            activateOrDeactivateSubmitButton(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);

        // Save the address string.
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        // We only start the service to fetch the address if GoogleApiClient is connected.
                        mAddressRequested = true;
                        updateUIWidgets();

                        fetchCurrentAddress();
                        mAddressRequested = false;
                        displayAddressOutput();
                        updateUIWidgets();
                        // If GoogleApiClient isn't connected, we process the user's request by setting
                        // mAddressRequested to true. Later, when GoogleApiClient connects, we launch the service to
                        // fetch the address. As far as the user is concerned, pressing the Fetch Address button
                        // immediately kicks off the process of getting the address.
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                    default:
                        break;
                }
                break;
        }
    }

    /**
     * Runs when user clicks the Use Current Address button. Starts the service to fetch the address if
     * GoogleApiClient is connected.
     */
    public void saveHomeLocality(View view) {
        runSubmit();
    }

    public void runSubmit() {
        if(mAddressOutput == null || mAddressOutput.trim().isEmpty()){
            mSearchHomeLocalityTxtView.setText("");
            mSearchHomeLocalityTxtView.didTouchFocusSelect();
            mSearchHomeLocalityTxtView.dismissDropDown();
            showToast("No locality entered. Please select locality within Bengaluru.");
        } else if(!mSearchHomeLocalityTxtView.getText().toString().trim().equalsIgnoreCase(mAddressOutput)) {
            mSearchHomeLocalityTxtView.setText("");
            mSearchHomeLocalityTxtView.didTouchFocusSelect();
            mSearchHomeLocalityTxtView.dismissDropDown();
            showToast("Please select valid locality within Bengaluru.");
        } else {
            triggerFinalSubmission();
        }
    }

    public void triggerFinalSubmission() {
        if(mAddressOutput != null && !mAddressOutput.trim().isEmpty() && mLastLocation != null) {
            if(mSearchHomeLocalityTxtView.getText().toString().trim().isEmpty()){
                Tlog.e("Please type your Home Locality");
                showToast("Please type your home locality (Ex: Bellandur)");
            } else {
                // submission only when address and lat/lng is available
                mHomeLocalityRequest.setCandidateMobile(Prefs.candidateMobile.get());
                mHomeLocalityRequest.setCandidateId(Prefs.candidateId.get());

                mHomeLocalityRequest.setLocalityName(mAddressOutput);
                mHomeLocalityRequest.setLat( mLastLocation.getLatitude());
                mHomeLocalityRequest.setLng( mLastLocation.getLongitude());
                if(mPlaceId!=null)mHomeLocalityRequest.setPlaceId(mPlaceId);

                mAsyncTask = new HomeLocalityAsyncTask();
                mAsyncTask.execute(mHomeLocalityRequest.build());

                /* update prefs values */
                Prefs.candidateHomeLat.put(String.valueOf(mLastLocation.getLatitude()));
                Prefs.candidateHomeLng.put(String.valueOf(mLastLocation.getLongitude()));

                mAddressRequested = false;
                showProgressBar = false;
                updateUIWidgets();
            }
        } else {
            Tlog.e("No/Invalid Location Provided");
            showToast("Please enter a valid locality within Bengaluru to search jobs (Ex: Bellandur)");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LatLngOrPlaceIdAsyncTask extends BasicLatLngOrPlaceIdAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar = true;
            pd.show();
            activateOrDeactivateSubmitButton(false);
            updateUIWidgets();
            Tlog.i("Fetching Locality Object from latlng....");
        }

        @Override
        protected void onPostExecute(LocalityObjectResponse localityObjectResponse) {
            super.onPostExecute(localityObjectResponse);
            if(localityObjectResponse!=null){
                pd.cancel();
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
                            /* since the req was made with latlng i.e the users latlng use that to make the final req*/
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
                    mSearchHomeLocalityTxtView.setText(mAddressOutput);
                    mSearchHomeLocalityTxtView.dismissDropDown();
                    mSearchHomeLocalityTxtView.clearFocus();
                    activateOrDeactivateSubmitButton(true);
                } else {
                    showToast("Error While Fetching Locality. Please manually type your locality above.");
                    mSearchHomeLocalityTxtView.setText("");
                    mAddressOutput = "";
                    mSearchHomeLocalityTxtView.clearFocus();
                }
                showProgressBar = false;
                updateUIWidgets();
            }
        }
    }

    private class HomeLocalityAsyncTask extends AsyncTask<HomeLocalityRequest,
            Void, HomeLocalityResponse> {

        protected void onPreExecute() {
            super.onPreExecute();
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
                Toast.makeText(getApplicationContext(), "Failed to set Home PlaceAPIHelper. Please try again.",
                        Toast.LENGTH_LONG).show();
                Log.w("","Null Response");
                return;
            } else if (homeLocalityResponse.getStatusValue() == ServerConstants.SUCCESS){
                Intent intent = new Intent(HomeLocality.this, SearchJobsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                Prefs.candidateHomeLocalityName.put(mAddressOutput);
                Prefs.candidateHomeLocalityStatus.put(ServerConstants.HOMELOCALITY_YES);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.no_change);
                finish();
            }
            else {
                showProgressBar=false;
                updateUIWidgets();
                if(!CheckNetworkStatus()){
                    showToast(ServerConstants.NETWORK_NOT_FOUND);
                }  else {
                    Tlog.e("Server issue !!");
                    showToast(ServerConstants.ERROR_WHILE_SAVING_HOME_LOCALITY);
                }
            }
        }
    }

}
