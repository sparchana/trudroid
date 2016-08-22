package in.trujobs.dev.trudroid;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import in.trujobs.dev.trudroid.Adapters.PlacesAutoCompleteAdapter;
import in.trujobs.dev.trudroid.CustomAsyncTask.BasicLatLngAsyncTask;
import in.trujobs.dev.trudroid.Helper.LatLngAPIHelper;
import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.Util.AsyncTask;
import in.trujobs.dev.trudroid.Util.Constants;
import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.dev.trudroid.api.ServerConstants;
import in.trujobs.proto.HomeLocalityRequest;
import in.trujobs.proto.HomeLocalityResponse;

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
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

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


    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;

    private static HomeLocalityRequest.Builder mHomeLocalityRequest = HomeLocalityRequest.newBuilder();


    private AsyncTask<HomeLocalityRequest, Void, HomeLocalityResponse> mAsyncTask;
    private AsyncTask<String, Void, LatLngAPIHelper> mLatLngAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_locality);
        setTitle("Hi " + Prefs.firstName.get() + "!");


        mResultReceiver = new AddressResultReceiver(new Handler());

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
                PlaceAPIHelper placeAPIHelper = (PlaceAPIHelper) parent.getItemAtPosition(position);
                mAddressOutput = placeAPIHelper.getDescription();
                Toast.makeText(HomeLocality.this, mAddressOutput, Toast.LENGTH_SHORT).show();
                mPlaceId = placeAPIHelper.getPlaceId();
                Tlog.i("mAddressOutput ------ " + mAddressOutput
                        + "\nplaceId:" + mPlaceId);
                showProgressBar = false;
                activateOrDeactivateSubmitButton(true);
                updateUIWidgets();
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
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
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
                startIntentService();
                if (mAddressOutput.equalsIgnoreCase(getString(R.string.service_not_available))) {
                    mAddressOutput = "";
                    showToast("Unable to detect location. Please turn on GPS in order to use this feature or manually type the location");
                }
                showProgressBar = false;
                updateUIWidgets();
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

    @SuppressLint("ParcelCreator")
    public class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            if(resultData.getString(Constants.RESULT_DATA_KEY).equalsIgnoreCase(getString(R.string.service_not_available))){
                Tlog.e("service_not_available string received onReceiveResult");
                showToast("Unable to detect location. Please turn on GPS in order to use this feature or manually type the location");
            } else {
                mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            }
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                showToast(getString(R.string.address_found));
            }

            // Reset. Enable the Fetch Address button and stop showing the progress bar.
            showProgressBar = false;
            mAddressRequested = false;
            updateUIWidgets();
        }
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
            case REQUEST_CODE_AUTOCOMPLETE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Get the user's selected place from the Intent.
                        Place place = PlaceAutocomplete.getPlace(this, data);
                        Tlog.i( "Place Selected: " + place.getName() + " Lat: "
                                + place.getLatLng().latitude + " lng: " + place.getLatLng().longitude
                                + "local" + place.getLocale() + " other: " + place.getAttributions());
                        // set final submission data
                        mAddressOutput = place.getName().toString();
                        Tlog.i( "gps LastLocation not available. setting to place lat/lng");
                        try{
                            mLastLocation.setLatitude(place.getLatLng().latitude);
                            mLastLocation.setLongitude(place.getLatLng().longitude);

                            mSearchHomeLocalityTxtView.setText(place.getName());
                        } catch (NullPointerException np){
                            mLastLocation = new Location("");
                            mLastLocation.setLatitude(place.getLatLng().latitude);
                            mLastLocation.setLongitude(place.getLatLng().longitude);
                        }
                        // Reset. Enable the Fetch Address button and stop showing the progress bar.
                        mAddressRequested = false;
                        displayAddressOutput();
                        updateUIWidgets();
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Status status = PlaceAutocomplete.getStatus(this, data);
                        Log.e(TAG, "Error: Status = " + status.toString());
                        break;
                    case Activity.RESULT_CANCELED:
                        // Indicates that the activity closed before a selection was made. For example if
                        // the user pressed the back button.
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
            showToast("No locality entered. Please select locality within Bengaluru.");
        }
        if(mPlaceId != null && GET_LOCALITY_FROM_AUTOCOMPLETE){
            showProgressBar = true;
            updateUIWidgets();
            Tlog.i("Triggering lat/lng fetch process with "+ mPlaceId);
            mLatLngAsyncTask = new LatLngAsyncTask();
            mLatLngAsyncTask.execute(mPlaceId);
            Tlog.i("lat/lng fetch completed");
            Tlog.i("skipped lat/lng fetch. Already provided via gps");
        } else {
            triggerFinalSubmission();
        }
    }

    public void triggerFinalSubmission(){
        if(mAddressOutput != null && !mAddressOutput.trim().isEmpty() && mLastLocation != null) {
            if(mSearchHomeLocalityTxtView.getText().toString().trim().isEmpty()){
                Tlog.e("Please type your Home Locality");
                showToast("Please type your home locality (Ex: Bellandur)");
            } else {
                // submission only when address and lat/lng is available
                mSearchHomeLocalityTxtView.setText(mAddressOutput);
                mSearchHomeLocalityTxtView.dismissDropDown();
                mHomeLocalityRequest.setCandidateMobile(Prefs.candidateMobile.get());
                mHomeLocalityRequest.setCandidateId(Prefs.candidateId.get());
                mHomeLocalityRequest.setAddress(mAddressOutput);

                mHomeLocalityRequest.setLat( mLastLocation.getLatitude());
                mHomeLocalityRequest.setLng( mLastLocation.getLongitude());

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
            showToast("Invalid Home Location Provided! Please enter a valid home locality (Ex: Bellandur)");
        }
    }

    private class LatLngAsyncTask extends BasicLatLngAsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Tlog.i("Fetching LatLng ....");
            mAddressRequested = true;
            updateUIWidgets();
        }

        @Override
        protected void onPostExecute(LatLngAPIHelper latLngAPIHelper) {
            super.onPostExecute(latLngAPIHelper);
            mLatLngAsyncTask = null;
            if(latLngAPIHelper!=null){
                if(mLastLocation == null){
                    mLastLocation = new Location("");
                    mLastLocation.setLatitude(latLngAPIHelper.getLatitude());
                    mLastLocation.setLongitude(latLngAPIHelper.getLongitude());
                }
                triggerFinalSubmission();
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
