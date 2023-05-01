package com.dripblood.myapplication.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dripblood.myapplication.R;
import com.dripblood.myapplication.activities.OtpActivity;
import com.dripblood.myapplication.activities.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpSendFragment extends Fragment {

    private EditText phone;
    private Button btnSend;
    private FirebaseAuth mAuth;
    View view;
    String verificationID;

    public OtpSendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp_send, container, false);

        Log.i("STEPS", "OTP Send");

        phone = view.findViewById(R.id.phone);
        btnSend = view.findViewById(R.id.btnSend);
        mAuth = FirebaseAuth.getInstance();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(phone.getText().toString()) || phone.getText().length() > 10 || phone.getText().length() < 9){
                    Toast.makeText(getActivity(), "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                }else{
                    String number = phone.getText().toString();
                    Bundle bundle = new Bundle();
                    bundle.putString("phone", number);

                    OtpVerifyFragment otpfragment = new OtpVerifyFragment();
                    otpfragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.otpFragmentContainer, otpfragment).addToBackStack(null).commit();
                }
            }
        });

        return view;
    }
}