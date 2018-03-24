package com.sayan.sample.androidtvlocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class MapLocation extends Activity implements OnMapReadyCallback {
    private static final int REQUEST_FOR_LOCATION = 1001;
    private static final int REQUEST_CHECK_SETTINGS = 1000;

    private ProgressBar mProgress;
    private final String TAG = "SayanDevice";

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private View mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location);
        initializeViews();
        mProgress.setVisibility(View.VISIBLE);
        Toast.makeText(this, "please wait", Toast.LENGTH_SHORT).show();
        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

//        if (isRunningOnTv()) {
//        initializeFusedLocationProviderClient();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                    checkLocation();
//                onClickAutoFetchLocation();
//            }
//        }, 2000);
//        } else {
//            Toast.makeText(this, "This is not a TV, please try on a TV", Toast.LENGTH_SHORT).show();
//        }
    }


    private void checkLocation() {
        // Request a static location from the location manager
        LocationManager locationManager = (LocationManager) this.getSystemService(
                Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Need Location permission", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = null;
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation("network");
            if (location != null) {
                Toast.makeText(this, "Latitude: " + location.getLatitude(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
// Attempt to get postal or zip code from the static location object
                Geocoder geocoder = new Geocoder(this);
                Address address = null;
                try {
                    address = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1).get(0);
                    Log.d("Zip code", address.getPostalCode());

                } catch (IOException e) {
                    Log.e(TAG, "Geocoder error", e);
                }
            } else {
                Toast.makeText(this, "Could not find your location", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    optForPlanB();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void optForPlanB() {
        List<ScanResult> results;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        results = wifiManager.getScanResults();

        String message = "No results found.Please Check your wireless is on";
        if (results != null) {
            final int size = results.size();
            if (size == 0)
                message = "No access points in range";
            else {
                ScanResult bestSignal = results.get(0);
                int count = 1;
                for (ScanResult result : results) {
                    if (WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0) {
                        bestSignal = result;
                        message = "Wifi available";
                    }
                    message = "Wifi available1";
                }
                Toast.makeText(this, bestSignal.operatorFriendlyName, Toast.LENGTH_LONG).show();
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            createLocationRequest();
            requestPermissionForLocation();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            createLocationRequest();
            requestPermissionForLocation();
        }
    }

    private void onClickAutoFetchLocation() {
        mProgress.setVisibility(View.VISIBLE);
        checkLocation();
    }

    private boolean isRunningOnTv() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            return true;
        } else {
            return false;
        }
    }

    private void initializeViews() {
        mProgress = (ProgressBar) findViewById(R.id.progress);
    }


    private void initializeFusedLocationProviderClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public boolean requestPermissionForLocation() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
// explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
//Toast.makeText(getApplicationContext(), "External storage permission is mandatory",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_FOR_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_FOR_LOCATION);
            }
            return true;
        } else {
            handleLocationRequestPermission();
            return false;
        }
    }

    private void handleLocationRequestPermission() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> task =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentLocation();
//                            raiseTicketWithLocation();
                        }
                    }, 3000);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    int code = exception.getStatusCode();
                    switch (code) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a progressDialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the progressDialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MapLocation.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            new AlertDialog.Builder(MapLocation.this)
                                    .setMessage("GPS is not enabled. Do you want to go to settings menu?")
                                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .setCancelable(false)
                                    .show();
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the progressDialog.
                            break;
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        Toast.makeText(this, "Fetching your current location...", Toast.LENGTH_SHORT).show();
        mFusedLocationClient.getLastLocation().addOnSuccessListener(MapLocation.this, new MyOnSuccessListener());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // TODO: Before enabling the My Location layer, you must request
        // location permission from the user. This sample does not include
        // a request for location permission.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "location permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "map ready", Toast.LENGTH_SHORT).show();
        mMap.setMyLocationEnabled(true);
//        googleMap.setPadding(0, 100, 0, 0);
        if (mMap.getMyLocation() != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), 17));
        }
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);rlp.setMargins(0,0,30,30);
    }

    private class MyOnSuccessListener implements OnSuccessListener<Location> {
        private int mOnSuccessCallCounter;

        @SuppressLint("MissingPermission")
        @Override
        public void onSuccess(Location location) {
            mOnSuccessCallCounter++;
            if (location != null) {
                doNextOperationAfterFetchingLocation(location);
            } else {
                if (mOnSuccessCallCounter <= 5) {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(MapLocation.this, this);
                } else {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(MapLocation.this, "Could not fetch your location, try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }


    }

    private void doNextOperationAfterFetchingLocation(Location location) {
        Toast.makeText(this, "Latitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Longitude: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }


    //not called for now, it is not working WIP
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d("sayan", " onrequestlocationpermission");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("sayan", " yes selected");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getCurrentLocation();
//                            raiseTicketWithLocation();
                        }
                    }, 3000);                    // permission was granted
//                    Toast.makeText(getApplicationContext(), "Location Permission granted", Toast.LENGTH_LONG).show();

                } else {
                    Log.d("sayan", " no selected");
                    mProgress.setVisibility(View.GONE);
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
//                    finish();
                }
                break;
            }
            case REQUEST_FOR_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
//                    Toast.makeText(getApplicationContext(), "SMS Permission granted", Toast.LENGTH_LONG).show();
                    handleLocationRequestPermission();
                } else {
//                    Toast.makeText(getApplicationContext(), "",Toast.LENGTH_LONG).show();
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "To fetch your location automatically, location permission is required", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Toast.makeText(this, "Location ON", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getCurrentLocation();
//                                raiseTicketWithLocation();
                            }
                        }, 3000);
                        // All required changes were successfully made
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this, "Location OFF", Toast.LENGTH_SHORT).show();
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
                break;
        }
    }

}
