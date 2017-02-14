package com.yadav.maps4;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
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
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yadav.maps4.SlidingInfo.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        ResultCallback<LocationSettingsResult>, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, PlaceSelectionListener, GoogleMap.OnCameraMoveStartedListener {

    private SlidingUpPanelLayout mLayout;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, mCurrentLocation;
    LocationRequest mLocationRequest;
    private boolean mResolvingError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int REQUEST_SELECT_PLACE = 1002;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    static final String DIALOG_ERROR = "dialog_error";
    static boolean searching = false;
    private ImageView imgMyLocation;
    double my_latitude,marker_lat;
    double my_longitude,marker_long;
    boolean isCameraMoved = false;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(28.1465251,76.5313287),new LatLng(29.1110565,77.5698790));
    Marker my_marker=null;
    Marker search_marker=null;
    Marker longClickMarker=null;
    FloatingActionButton frag_nav=null;
    FloatingActionButton nav=null;
    TextView infoTitle=null;
    TextView tv2=null;
    int time=0;
    private static String KEY_SUCCESS = "success";
    private static String KEY_LOCATIONS = "locations";
    private static String KEY_LATITUDE = "latitude";
    private static String KEY_LONGITUDE = "longitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        imgMyLocation = (ImageView) findViewById(R.id.my_loc_img);
        imgMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searching=false;
                getMyLocation();
            }
        });
        Toolbar myToolBar = (Toolbar) findViewById(R.id.my_toolbar);
       setSupportActionBar(myToolBar);
        //getSupportActionBar()
        //myToolBar.inflateMenu(R.menu.maps_menu);
        //myToolBar.setTitle("My Location");

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i("panel event", "onPanelSlide, offset " + slideOffset);
                if(slideOffset < 0.175){
                    nav.setVisibility(View.INVISIBLE);
                }else{
                    nav.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i("panel event", "onPanelStateChanged " + newState);

            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);




            }
        });

        infoTitle = (TextView) findViewById(R.id.infoTitle);
       infoTitle.setText("title");
        tv2 = (TextView) findViewById(R.id.tv2);
        tv2.setText("info");
       /* tv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MapsActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();
            }
        });
        List<String> your_array_list = Arrays.asList(
                "This",
                "Is",
                "An",
                "Example");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                your_array_list );

        tv2.setAdapter(arrayAdapter);  */

        nav = (FloatingActionButton) findViewById(R.id.navigation);
        nav.setImageResource(R.drawable.route);
        nav.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3366ff")));
      //  nav.setText("nav");
      //  nav.setMovementMethod(LinkMovementMethod.getInstance());
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click","navigation");
             /*   Uri gmmUri = Uri.parse("google.navigation:q="+marker_lat+","+marker_long);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,gmmUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);  */

                String uri = "http://maps.google.com/maps?saddr="+my_latitude+","+my_longitude+"&daddr="+marker_lat+","+marker_long;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
                mapIntent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                startActivity(mapIntent);
            }
        });

        frag_nav = (FloatingActionButton) findViewById(R.id.frag_navB);
        frag_nav.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3366ff")));
        frag_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click","navigation");
             /*   Uri gmmUri = Uri.parse("google.navigation:q="+marker_lat+","+marker_long);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,gmmUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);  */

                String uri = "http://maps.google.com/maps?saddr="+my_latitude+","+my_longitude+"&daddr="+marker_lat+","+marker_long;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(uri));
                mapIntent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
                startActivity(mapIntent);
            }
        });

        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.setAnchorPoint(0.4f);

        Log.d("on create","no reslve");
        if(mGoogleApiClient==null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build();
        }

    }

public void getMyLocation(){

    LatLng latLng = new LatLng(my_latitude, my_longitude);
    String myLoc = "lat : " + my_latitude + ", long : " + my_longitude;
    if(my_marker == null) {
        my_marker = mMap.addMarker(new MarkerOptions().position(latLng));
    }
    my_marker.setPosition(latLng);
    my_marker.setTitle(myLoc);
    my_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
    my_marker.setSnippet("my Location");
    CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(latLng, 14);
    mMap.animateCamera(loc);
    Log.d("my loc","button........");
    new FetchParkingLoc().execute();
   // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

}
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("on connected","no reslve");
        createLocationRequest();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            builder.setAlwaysShow(true);
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                            builder.build());

            result.setResultCallback(MapsActivity.this);

        // if (mRequestingLocationUpdates) {
        //     startLocationUpdates();
        //}
/*
        if (mLastLocation != null) {
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startIntentService(mLastLocation);
            }
        }
        */
            /*if (mLastLocation != null) {
                mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
                mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            }*/

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //  result.setResultCallback(new ResultCallback<LocationSettingsResult>()) {

        //  });

    }

    @Override
    public void onResult(LocationSettingsResult result) {
        final Status status = result.getStatus();
        Log.d("on result","no reslve");
        // final LocationSettingsStates= result.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.d("result","success");
                accessLocationServices();

                // All location settings are satisfied. The client can
                // initialize location requests here
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Log.d("result","resolution required");
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            MapsActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.d("result","no reslve");
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                break;
        }
    }

    private void accessLocationServices() {
        //double latitude;
        //double longitude;
        if(my_marker == null) {
            Log.d("marker","Add");
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                //mRequestingLocationUpdates = true;
            } else {
                Toast.makeText(MapsActivity.this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            }
            Log.d("on connected", "before check");
            if (mLastLocation != null) {
                Log.d("on connected", "in ckeck");
                my_latitude = mLastLocation.getLatitude();
                my_longitude = mLastLocation.getLongitude();
            } else {
                my_latitude = 28.4971555;
                my_longitude = 77.1694188;
            }
            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(my_latitude, my_longitude);
            String myLoc = "lat : " + my_latitude + ", long : " + my_longitude;
            my_marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Start").snippet(myLoc));
            // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(loc);
        }

            //start vhecking for location change
            startLocationUpdates();

    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            Toast.makeText(MapsActivity.this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        // mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //startIntentService(location);
        updateUI(mCurrentLocation);
    }

    public void updateUI(Location location)
    {
        if (location != null) {

            my_latitude = location.getLatitude();
            my_longitude = location.getLongitude();
            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(my_latitude, my_longitude);
        /*    if(my_marker != null)
                my_marker.remove();
            else
                time=1;
            my_marker = mMap.addMarker(new MarkerOptions().position(latLng).title("Start"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            if(time == 1) {
                CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                mMap.animateCamera(loc);
                time=0;
            }
            else
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));  */
            if(my_marker == null) {
                my_marker = mMap.addMarker(new MarkerOptions().position(latLng));
            }
                my_marker.setPosition(latLng);
                String myLoc = "lat : " + my_latitude + ", long : " + my_longitude;
                my_marker.setTitle(myLoc);
                my_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car));
                my_marker.setSnippet("my Location");

           // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //RR

            if(!searching) {
                if (time == 0) {
                    CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(latLng, 14);
                    mMap.animateCamera(loc);
                    new FetchParkingLoc().execute();
                    time = 1;
                } else
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

        }
    }

    @Override
    public void onConnectionSuspended(int connectionHint) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
           // mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            Toast.makeText(MapsActivity.this, "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
        }

     /*   mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                searching=false;
                getMyLocation();
                return true;
            }
        }); */

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if(longClickMarker != null) {
                    longClickMarker.remove();
                }

                ///////////////////////////////////////////////
                Geocoder geoCoder = new Geocoder(
                        getBaseContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geoCoder.getFromLocation(
                            latLng.latitude, latLng.longitude,1);
                    Log.d("list","ädre....................");
                    String add = "";
                    if (addresses.size() > 0)
                    {
                        Log.d("Add", "äddress" + addresses.get(0).getAddressLine(0).toString());
                        for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();
                             i++)
                            add += addresses.get(0).getAddressLine(i) + "\n";
                    }

                    Toast.makeText(getBaseContext(), add, Toast.LENGTH_LONG).show();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                    String myLoc = "lat : " + latLng.latitude + ", long : " + latLng.longitude;
                    longClickMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(myLoc));
                    show_sliding_info(longClickMarker,SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng latLng){
                if(longClickMarker != null){
                    longClickMarker.remove();
                    longClickMarker=null;

                    frag_nav.setVisibility(View.GONE);
                }

                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });

        mMap.setOnMarkerClickListener(MapsActivity.this);
        mMap.setOnCameraMoveStartedListener(this);

/*
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                LinearLayout infoView = new LinearLayout(MapsActivity.this);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                infoParams.gravity= Gravity.BOTTOM;
                infoView.setLayoutParams(infoParams);
                TextView latitude = new TextView(MapsActivity.this);
                latitude.setText("lat:");
                TextView longitude = new TextView(MapsActivity.this);
                longitude.setText("long :");
                infoView.addView(latitude);
                infoView.addView(longitude);

                return infoView;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });   */
     /*   mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
               // searching=true;
            }
        });    */


        /**Marker syd;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        syd=mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").snippet("Kiel is cool"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
      //  syd.setTag(0);

        mMap.setOnMarkerClickListener(this);
*/

    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        Log.d("my id",my_marker.getId());
        Log.d("marker id",marker.getId());
        if(marker.getId().equals(my_marker.getId())){
            if(longClickMarker != null)
            longClickMarker.remove();
            show_sliding_info(marker,SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else {
           /* if(search_marker != null ){
                if(marker.getId().equals(search_marker.getId())) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    return true;
                }
            } */

            show_sliding_info(marker,SlidingUpPanelLayout.PanelState.ANCHORED);
            CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
            mMap.animateCamera(loc);

            marker_lat = marker.getPosition().latitude;
            marker_long = marker.getPosition().longitude;
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                Toast.makeText(MapsActivity.this, "user select ok.....", Toast.LENGTH_LONG).show();
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                accessLocationServices();
            }
            else if( resultCode == RESULT_CANCELED ){
                Toast.makeText(MapsActivity.this, "user select no!!!!", Toast.LENGTH_LONG).show();
                mGoogleApiClient.disconnect();
                finish();
            }
        }
        else if ( requestCode == REQUEST_SELECT_PLACE){
            Log.d("onresult","select place");
            if(resultCode == RESULT_OK){
                Place place = PlaceAutocomplete.getPlace(this,data);

                this.onPlaceSelected(place);
            }
            else if(resultCode == PlaceAutocomplete.RESULT_ERROR){
                Status status = PlaceAutocomplete.getStatus(this,data);
                Log.d("status",status.toString());
                this.onError(status);
            }
        }
    }

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapsActivity) getActivity()).onDialogDismissed();
        }
    }

   @Override
    public void onStart(){
       super.onStart();
     //  boolean isON = checkGPS();
      // if(isON)
       mGoogleApiClient.connect();
   }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //show menu items in toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.maps_menu,menu);
        return true;
    }
    //on click toolbar widget
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.search_bar){
            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(MapsActivity.this);
                    //    .setBoundsBias(BOUNDS_MOUNTAIN_VIEW).build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_SELECT_PLACE);
            }catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlaceSelected(Place place){
        searching = true;
        mMap.clear();
        my_marker = null;
        //if(search_marker == null) {
            search_marker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
            show_sliding_info(search_marker,SlidingUpPanelLayout.PanelState.COLLAPSED);
     /*   }
        else{
            search_marker.setPosition(place.getLatLng());
            search_marker.setTitle(place.getName().toString());
        }    */

        // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15);
        mMap.animateCamera(loc);

        new FetchParkingLoc().execute();
    }

    @Override
    public void onError(Status status){

    }


    /**
     * Async Task to get and send data to My Sql database through JSON respone.
     **/
    private class FetchParkingLoc extends AsyncTask<Void, Void, JSONObject> {
        String latitude;
        String longitude;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Double Dlatitude;
            Double Dlongitude;
            if(searching){
                Dlatitude = search_marker.getPosition().latitude;
                Dlongitude = search_marker.getPosition().longitude;

            }else {
                Dlatitude = my_latitude;
                Dlongitude = my_longitude;
            }

            latitude = Dlatitude.toString();
            longitude = Dlongitude.toString();
        }

        @Override
        protected JSONObject doInBackground(Void... args) {

             ServerConnection conn = new ServerConnection();
            JSONObject json = conn.getNearByParkings(latitude,longitude);
            /* [ "locations" : {
                    "0" :{
                            "lat" : "123"
                            "long" : "456"
                        },
                    "1" :{
                             "lat" : "765"
                             "long" : "546"
                         }
                 } ]
             */
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1){

                        JSONObject locs_json = json.getJSONObject(KEY_LOCATIONS);

                        Iterator<String> keys = locs_json.keys();
                        while ( keys.hasNext()){
                            String key = keys.next();
                            Log.d("key",key);
                            JSONObject json_park = locs_json.getJSONObject(key);
                            Double park_latitude = json_park.getDouble(KEY_LATITUDE);
                            Double park_longitude = json_park.getDouble(KEY_LONGITUDE);
                            String store_name = json_park.getString("name");
                            String store_add = json_park.getString("store_add");
                            LatLng latLng = new LatLng(park_latitude,park_longitude);
                            mMap.addMarker(new MarkerOptions().position(latLng).title(store_name).snippet(store_add).icon(BitmapDescriptorFactory.fromResource(R.drawable.park)));
                        }

                    }else{
                        Toast.makeText(MapsActivity.this, "Unable to contact server......!!!", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void show_sliding_info(Marker marker, SlidingUpPanelLayout.PanelState state) {
        mLayout.setPanelState(state);
        frag_nav.setVisibility(View.VISIBLE);
        nav.setVisibility(View.GONE);
        marker_lat = marker.getPosition().latitude;
        marker_long = marker.getPosition().longitude;
        infoTitle.setText(marker.getTitle());
        tv2.setText(marker.getSnippet());
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            Log.d("gesture","user gesture........................");
            searching=true;
        }
        /*else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            //searching=true;
            Log.d("user","user action-------------------------------");
            Toast.makeText(this, "The user tapped something on the map.",
                    Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            Toast.makeText(this, "The app moved the camera.",
                    Toast.LENGTH_SHORT).show();
            Log.d("app","app moved/////////////////////////////////");
        }            */
    }


    @Override
    public void onBackPressed(){
        if(mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            frag_nav.setVisibility(View.GONE);

                longClickMarker = null;
                my_marker = null;
            mMap.clear();
            searching=false;
        }
        else {
            this.finish();
        }
    }
}
