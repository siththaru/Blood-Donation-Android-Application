package com.dripblood.myapplication.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dripblood.myapplication.R;
import com.dripblood.myapplication.activities.LoginActivity;
import com.dripblood.myapplication.activities.ProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerifyFragment extends Fragment {

    private String otp;
    private EditText otp1,otp2,otp3,otp4,otp5,otp6;
    private Button btnVerify;
    private TextView txtnumber, resend;
    private FirebaseAuth mAuth;
    private AuthCredential googleCredential;
    private View view;
    private String verificationID;
    private ProgressDialog pd;
    PhoneAuthProvider.ForceResendingToken resendToken;

    private String email;
    private String password;
    private String phone;

    public OtpVerifyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp_verify, container, false);

        Log.i("STEPS", "OTP Verify");

        Intent i = getActivity().getIntent();
        email = i.getStringExtra("EMAIL");
        password = i.getStringExtra("PASSWORD");

        googleCredential = LoginActivity.gCred;

        Bundle bundle = getArguments();
        phone = bundle.getString("phone");

        otp1 = view.findViewById(R.id.etC1);
        otp2 = view.findViewById(R.id.etC2);
        otp3 = view.findViewById(R.id.etC3);
        otp4 = view.findViewById(R.id.etC4);
        otp5 = view.findViewById(R.id.etC5);
        otp6 = view.findViewById(R.id.etC6);

        btnVerify = view.findViewById(R.id.btnVerify);
        txtnumber = view.findViewById(R.id.tvMobile);
        resend = view.findViewById(R.id.tvResendBtn);
        mAuth = FirebaseAuth.getInstance();

        txtnumber.setText("+94"+phone);

        pd = new ProgressDialog(getActivity());
        pd.show();
        pd.setContentView(R.layout.progress_dialog);
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        sendVerificationCode(phone);

        Toast.makeText(getActivity(), "OTP Sent Successfully", Toast.LENGTH_SHORT).show();
        pd.dismiss();

        editTextInput();

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pd.show();
                resendOTP(phone);
                Toast.makeText(getActivity(), "OTP Re-sent Successfully", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txtOtp1 = otp1.getText().toString();
                String txtOtp2 = otp2.getText().toString();
                String txtOtp3 = otp3.getText().toString();
                String txtOtp4 = otp4.getText().toString();
                String txtOtp5 = otp5.getText().toString();
                String txtOtp6 = otp6.getText().toString();

                if(txtOtp1.isEmpty() || txtOtp2.isEmpty() || txtOtp3.isEmpty() || txtOtp4.isEmpty() || txtOtp5.isEmpty() || txtOtp6.isEmpty()){
                    Toast.makeText(getActivity(), "Invalid OTP !", Toast.LENGTH_SHORT).show();
                }else {
                    otp = txtOtp1 + txtOtp2 + txtOtp3 + txtOtp4 + txtOtp5 + txtOtp6;
                    verifyCode(otp);
                }
            }
        });

        return view;
    }

    private void editTextInput() {
        otp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otp2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otp3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otp4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otp5.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                otp6.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+94"+phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(getActivity())                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendOTP(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+94"+phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(getActivity())                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .setForceResendingToken(resendToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            final String code = credential.getSmsCode();
            if(code!=null){
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getActivity(), "Verification Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);

            verificationID = s;
            resendToken = token;
        }
    };

    private void verifyCode(String Code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, Code);
        signinbyCredentials(credential);

    }

    private void signinbyCredentials(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "OTP Successfully Verified!", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();

                    if (googleCredential != null) {
                        repeatGooglesignIn();
                    }else {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("PASSWORD", password);
                        intent.putExtra("PHONE", phone);
                        startActivity(intent);
                    }

                }else{
                    Toast.makeText(getActivity(), "OTP is not Valid!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void repeatGooglesignIn(){
        FirebaseAuth.getInstance().signInWithCredential(googleCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

//                    Toast.makeText(getActivity(), "Google Login Success.", Toast.LENGTH_SHORT).show();

                    Log.i("CURID_2", "Google Login Success");
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("PASSWORD", password);
                    intent.putExtra("PHONE", phone);
                    startActivity(intent);

                } else {

                    Toast.makeText(getActivity(), "Google Login Failed.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

}