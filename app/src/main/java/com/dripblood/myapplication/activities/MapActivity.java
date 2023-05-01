package com.dripblood.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.dripblood.myapplication.R;
import com.dripblood.myapplication.viewmodels.CustomUserData;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import java.util.Map;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1312;
    private Marker marker_me;
    private Marker patientMarker;
    private LatLng latLng;
    private LatLng curLocation;
    private DatabaseReference posts_ref;
    private Map<Marker, CustomUserData> markerMap;

    private Polyline polyline;

    private final int MY_PERMISSION_REQUEST_SEND_SMS =1;

    private final String SENT="SMS_SENT";
    private final String DELIVERED="SMS_DELIVERED";

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText txtMsg;
    private Button btnSendSMS;

    PendingIntent sendPI,deliveredPI;
    BroadcastReceiver smsSentReciever, smsDeliveredReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        posts_ref = FirebaseDatabase.getInstance().getReference();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Blood Points");

        sendPI= PendingIntent.getBroadcast(MapActivity.this,0,new Intent(SENT),0);
        deliveredPI=PendingIntent.getBroadcast(MapActivity.this,0,new Intent(DELIVERED),0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @SuppressLint({"MissingPermission", "PotentialBehaviorOverride"})
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        AddPostsMarkers();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        //Targeting Sri Lanka
        LatLng latLng = new LatLng(7.8731, 80.7718);
        CameraPosition cameraPosition = CameraPosition.builder().target(latLng).zoom(7f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);

        //Checking Permissions
        if (checkPermissions()) {
            CheckGps();
            map.setMyLocationEnabled(true);
        }else{
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

        //Clicking location button
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                CheckGps();
                return false;
            }
        });

        //Clicking marker on map
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                    CustomUserData patientData = markerMap.get(marker);

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MapActivity.this, R.style.BottomSheetDialogTheme);
                    View bottomSheetView = LayoutInflater.from(getApplicationContext())
                            .inflate(R.layout.layout_bottom_sheet, (LinearLayout) findViewById(R.id.bottomSheetContainer));

                    TextView recpName = bottomSheetView.findViewById(R.id.recpName);
                    recpName.setText(patientData.getName());

                    TextView recpBloodGrp = bottomSheetView.findViewById(R.id.recpBloodGrp);
                    recpBloodGrp.setText("Needs Blood " + patientData.getBloodGroup());

                    TextView recpContact = bottomSheetView.findViewById(R.id.recpContact);
                    recpContact.setText(patientData.getContact());

                    bottomSheetView.findViewById(R.id.recpDirection).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getDirections(marker_me.getPosition(), marker.getPosition());
                            bottomSheetDialog.dismiss();
                        }
                    });

                    bottomSheetView.findViewById(R.id.recpMessage).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupMessageDialog(patientData.getContact());
                        }
                    });

                    bottomSheetView.findViewById(R.id.recpCall).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String num = patientData.getContact();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + num));
                            startActivity(intent);
                        }
                    });

                    bottomSheetDialog.setContentView(bottomSheetView);
                    bottomSheetDialog.show();

                return false;
            }
        });
    }

    private void AddPostsMarkers() {

        markerMap = new HashMap<Marker, CustomUserData>();

        Query allposts = posts_ref.child("posts");
        allposts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {

                    for (DataSnapshot singlepost : dataSnapshot.getChildren()) {
                        for (DataSnapshot singchildlepost : singlepost.getChildren()) {
                            CustomUserData customUserData = singchildlepost.getValue(CustomUserData.class);

                            String patientName = customUserData.getName();
                            latLng = new LatLng(Double.parseDouble(customUserData.getLatitude()), Double.parseDouble(customUserData.getLongitude()));
                            MarkerOptions markerOptionsShop = new MarkerOptions();
                            markerOptionsShop.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_mark));
                            markerOptionsShop.title(patientName);
                            markerOptionsShop.position(latLng);
                            patientMarker=map.addMarker(markerOptionsShop);

                            markerMap.put(patientMarker, customUserData);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.d("User", databaseError.getMessage());

            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getCurLocation(){
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(5000)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                        super.onLocationAvailability(locationAvailability);

                    }

                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        Location lastlocation = locationResult.getLastLocation();

                        LatLng latLng = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                        curLocation = latLng;
                        if (marker_me == null){
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title("You");
                            markerOptions.position(latLng);
                            marker_me = map.addMarker(markerOptions);
                            cameraAnimate();
                        }else{
                            marker_me.setPosition(latLng);
                        }

                    }
                }, Looper.getMainLooper());

    }

    private void cameraAnimate(){
        CameraPosition cameraPosition = CameraPosition.builder().target(curLocation).zoom(13f).build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }

    public void getDirections(LatLng start,LatLng end){
        OkHttpClient client = new OkHttpClient();
        String URL="https://maps.googleapis.com/maps/api/directions/json?origin="
                +start.latitude
                +","
                +start.longitude
                +"&destination="
                +end.latitude
                +","
                +end.longitude
                +"&key="
                +getString(R.string.directions_api_key);

        Request request = new Request.Builder().url(URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("NewApi")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String json = response.body().string();

                try{
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray routes = jsonObject.getJSONArray("routes");


                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");

                    List<LatLng> points = PolyUtil.decode(overviewPolyline.getString("points"));


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(polyline==null){

                                PolylineOptions polylineOptions = new PolylineOptions();
                                polylineOptions.width(15);
                                polylineOptions.color(getColor(R.color.color_road));
                                polylineOptions.addAll(points);
                                polyline = map.addPolyline(polylineOptions);

                            }else {
                                polyline.setPoints(points);
                            }
                        }
                    });

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void CheckGps() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(3000)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        Task<LocationSettingsResponse>locationSettingsResponseTask = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    getCurLocation();
                } catch (ApiException e) {
                    if(e.getStatusCode()== LocationSettingsStatusCodes.RESOLUTION_REQUIRED){
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        try {
                            resolvableApiException.startResolutionForResult(MapActivity.this, LOCATION_PERMISSION_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if(e.getStatusCode()== LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE){
                        Toast.makeText(MapActivity.this, "Settings not available", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    public boolean checkPermissions(){
        boolean permission = false;
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            permission = true;
        }
        return permission;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            for(int i = 0; i < permissions.length; i++){
                if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                }else if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                }
            }
            if (permissionGranted){
                CheckGps();
                map.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (resultCode==RESULT_OK){
                getCurLocation();
                Toast.makeText(this, "Gps is enabled", Toast.LENGTH_SHORT).show();
            }else if(resultCode==RESULT_CANCELED){
                Toast.makeText(this, "Denied Gps enable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(smsSentReciever);
        unregisterReceiver(smsDeliveredReciever);
    }

    @Override
    protected void onResume(){
        super.onResume();
        smsSentReciever =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context,"SMS Sent Successfully",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context,"No Service",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context,"Null PDU",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context,"Airplane Mode / Radio Off",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsDeliveredReciever=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context,"SMS Delivered",Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_CANCELLED:
                        Toast.makeText(context,"SMS Not Delivered",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        registerReceiver(smsSentReciever,new IntentFilter(SENT));
        registerReceiver(smsDeliveredReciever,new IntentFilter(DELIVERED));
    }

    public void popupMessageDialog(String phone){
        dialogBuilder = new AlertDialog.Builder(this);
        final View messagePopup = getLayoutInflater().inflate(R.layout.popup, null);

        txtMsg = (EditText) messagePopup.findViewById(R.id.txtArea);
        btnSendSMS = (Button) messagePopup.findViewById(R.id.sendSMS);

        dialogBuilder.setView(messagePopup);
        dialog = dialogBuilder.create();
        dialog.show();

        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = txtMsg.getText().toString();
                String telNr = phone;

                if(message.isEmpty()){
                    Toast.makeText(MapActivity.this, "Please Enter a Message", Toast.LENGTH_SHORT).show();
                }else {
                    if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST_SEND_SMS);
                    } else {
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(telNr, null, message, sendPI, deliveredPI);
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}