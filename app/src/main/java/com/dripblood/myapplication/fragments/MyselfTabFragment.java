package com.dripblood.myapplication.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dripblood.myapplication.R;
import com.dripblood.myapplication.activities.Dashboard;
import com.dripblood.myapplication.activities.LoginActivity;
import com.dripblood.myapplication.viewmodels.UserData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MyselfTabFragment extends Fragment{

    ProgressDialog pd;

    TextView locationBtn;
    Button btnpost;

    FirebaseDatabase fdb;
    DatabaseReference db_ref;
    FirebaseAuth mAuth;

    Calendar cal;
    String uid;
    String Time, Date;

    String latitude;
    String longitude;

    FusedLocationProviderClient client;

    float v = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.myself_tab_fragment, container, false);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel channel = new NotificationChannel("Dripblood Notification", "Dripblood", NotificationManager.IMPORTANCE_DEFAULT);
//            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel);
//        }

        pd = new ProgressDialog(getActivity());
        pd.show();
        pd.setContentView(R.layout.progress_dialog);
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pd.dismiss();

        client = LocationServices.getFusedLocationProviderClient(getActivity());

        locationBtn = root.findViewById(R.id.btnlocation);
        btnpost = root.findViewById(R.id.btnpost);
        cal = Calendar.getInstance();

        locationBtn.setTranslationX(800);
        btnpost.setTranslationX(800);

        locationBtn.setAlpha(v);
        btnpost.setAlpha(v);

        locationBtn.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        btnpost.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(500).start();

        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int hour = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);
        month+=1;
        Time = "";
        Date = "";
        String ampm="AM";

        if(cal.get(Calendar.AM_PM) ==1) { ampm = "PM";}

        if(hour<10) { Time += "0"; }

        Time += hour;
        Time +=":";

        if(min<10) { Time += "0"; }

        Time +=min;
        Time +=(" "+ampm);

        Date = day+"/"+month+"/"+year;

        FirebaseUser cur_user = mAuth.getInstance().getCurrentUser();

        if(cur_user == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        } else {
            uid = cur_user.getUid();
        }

        mAuth = FirebaseAuth.getInstance();
        fdb = FirebaseDatabase.getInstance();
        db_ref = fdb.getReference("posts");

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ){
                    getCurrentLocation();
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Location Set Success", Toast.LENGTH_SHORT).show();
                }else{
                    pd.dismiss();
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                        ,Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                }
            }
        });

        try {
            btnpost.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                        pd.show();
                        final Query findname = fdb.getReference("users").child(uid);

                        if (latitude==null && longitude==null) {
                            Toast.makeText(getActivity(), "Please set your location!", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        } else {

                            findname.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {
                                        db_ref.child(uid).child("Myself").child("Name").setValue(dataSnapshot.getValue(UserData.class).getName());
                                        db_ref.child(uid).child("Myself").child("Contact").setValue(dataSnapshot.getValue(UserData.class).getContact());
                                        db_ref.child(uid).child("Myself").child("Latitude").setValue(latitude);
                                        db_ref.child(uid).child("Myself").child("Longitude").setValue(longitude);
                                        db_ref.child(uid).child("Myself").child("BloodGroup").setValue(String.valueOf(dataSnapshot.getValue(UserData.class).getBloodGroup()));
                                        db_ref.child(uid).child("Myself").child("Time").setValue(Time);
                                        db_ref.child(uid).child("Myself").child("Date").setValue(Date);
                                        Toast.makeText(getActivity(), "Your post has been created successfully", Toast.LENGTH_LONG).show();

                                        startActivity(new Intent(getActivity(), Dashboard.class));

                                        createNotif();

                                    } else {
                                        Toast.makeText(getActivity(), "Database error occured.", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("User", databaseError.getMessage());
                                }
                            });
                        }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        pd.dismiss();

        return root;
    }

    private void createNotif(){
        String id = "my_channel_id_1";

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = manager.getNotificationChannel(id);
            if (channel == null){
                channel = new NotificationChannel(id, "DriplBlood", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Your post has been successfully placed");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 1000, 200, 340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }

        Intent notificationIntent = new Intent(getActivity(), Dashboard.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), id);
        builder.setContentTitle("DripBlood Post");
        builder.setContentText("Your post is been successfully placed");
        builder.setSmallIcon(R.drawable.logo);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setVibrate(new long[]{100, 1000, 200, 340});
        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getActivity());
        managerCompat.notify(1, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100 && (grantResults.length > 0) &&
                (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            getCurrentLocation();
            Toast.makeText(getActivity(), latitude+" + "+longitude, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if(location != null){
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                    }else{
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                latitude = String.valueOf(location1.getLatitude());
                                longitude = String.valueOf(location1.getLongitude());
                            }

                            @Override
                            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {

                            }
                        };
                        client.requestLocationUpdates(locationRequest
                            ,locationCallback, Looper.myLooper());
                    }
                }
            });
        }else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }


}
