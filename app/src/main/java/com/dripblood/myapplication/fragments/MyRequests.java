package com.dripblood.myapplication.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dripblood.myapplication.R;
import com.dripblood.myapplication.activities.Dashboard;
import com.dripblood.myapplication.viewmodels.CustomUserData;
import com.dripblood.myapplication.viewmodels.DonorData;
import com.dripblood.myapplication.viewmodels.UserData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.TimeZone;

public class MyRequests extends Fragment {

    private ProgressDialog pd;
    DatabaseReference db_ref;
    FirebaseAuth mAuth;

    private TextView bloodGrp1, bloodGrp2;
    private TextView contactNo1, contactNo2;
    private TextView posted1, posted2;
    private TextView noReqMsg1, noReqMsg2;
    private LinearLayout myselfLayout, myfamilyLayout;

    private ImageView del1, del2;

    private View view;

    public MyRequests() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_requests, container, false);

        getActivity().setTitle("My Requests");

        pd = new ProgressDialog(getActivity());
        pd.show();
        pd.setContentView(R.layout.progress_dialog);
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pd.dismiss();

        bloodGrp1 = view.findViewById(R.id.targetBG);
        bloodGrp2 = view.findViewById(R.id.targetBG1);
        contactNo1 = view.findViewById(R.id.targetCN);
        contactNo2 = view.findViewById(R.id.targetCN1);
        posted1 = view.findViewById(R.id.posted);
        posted2 = view.findViewById(R.id.posted1);
        del1 = view.findViewById(R.id.del);
        del2 = view.findViewById(R.id.del1);
        noReqMsg1 = view.findViewById(R.id.noReq1);
        noReqMsg2 = view.findViewById(R.id.noReq2);
        myselfLayout = view.findViewById(R.id.myselfSet);
        myfamilyLayout = view.findViewById(R.id.myFamilySet);

        mAuth  = FirebaseAuth.getInstance();
        db_ref = FirebaseDatabase.getInstance().getReference("posts");

        pd.show();

        Query myself = db_ref.child(mAuth.getCurrentUser().getUid()).child("Myself");

        myself.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    noReqMsg1.setVisibility(View.GONE);

                    CustomUserData customUserData1 = dataSnapshot.getValue(CustomUserData.class);
                    bloodGrp1.setText(customUserData1.getBloodGroup());
                    contactNo1.setText(customUserData1.getContact());
                    posted1.setText("Posted "+customUserData1.getTime()+" "+customUserData1.getDate());
                }else {
                    noReqMsg1.setVisibility(View.VISIBLE);
                    myselfLayout.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("User", databaseError.getMessage());
            }

        });

        Query myfamily = db_ref.child(mAuth.getCurrentUser().getUid()).child("Myfamily");

        myfamily.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    noReqMsg2.setVisibility(View.GONE);

                    CustomUserData customUserData2 = dataSnapshot.getValue(CustomUserData.class);

                    bloodGrp2.setText(customUserData2.getBloodGroup());
                    contactNo2.setText(customUserData2.getContact());
                    posted2.setText("Posted "+customUserData2.getTime()+" "+customUserData2.getDate());
                }else {
                    noReqMsg2.setVisibility(View.VISIBLE);
                    myfamilyLayout.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("User", databaseError.getMessage());
            }

        });
    pd.dismiss();

    del1.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            db_ref.child(mAuth.getCurrentUser().getUid()).child("Myfamily").removeValue();
        }
    });

    del2.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            db_ref.child(mAuth.getCurrentUser().getUid()).child("Myself").removeValue();
        }
    });

        return view;
    }
}