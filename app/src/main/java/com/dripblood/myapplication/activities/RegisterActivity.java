package com.dripblood.myapplication.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dripblood.myapplication.R;
import com.dripblood.myapplication.fragments.AboutUs;
import com.dripblood.myapplication.fragments.AchievmentsView;
import com.dripblood.myapplication.fragments.OtpSendFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputemail, inputpassword, retypePassword;
    private Button btnSignup;
    private ProgressDialog pd;
    private DatabaseReference db_user;

    String email, password, ConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar
        this.setTitle("Registration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dialog
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        //FirebaseDatabase Reference
        db_user = FirebaseDatabase.getInstance().getReference("users");

        inputemail = findViewById(R.id.input_userEmail);
        inputpassword = findViewById(R.id.input_password);
        retypePassword = findViewById(R.id.input_password_confirm);
        btnSignup = findViewById(R.id.button_register);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                email = inputemail.getText().toString();
                password = inputpassword.getText().toString();
                ConfirmPassword = retypePassword.getText().toString();

                if (email.length() == 0) {
                    ShowError("Email ID");
                    inputemail.requestFocusFromTouch();
                } else if (password.length() <= 5) {
                    ShowError("Password");
                    inputpassword.requestFocusFromTouch();
                } else if (password.compareTo(ConfirmPassword) != 0) {
                    Toast.makeText(RegisterActivity.this, "Password did not match!", Toast.LENGTH_LONG).show();
                    retypePassword.requestFocusFromTouch();
                } else {
                    db_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            boolean present = false;

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                String singleEmail = dataSnapshot.child("Email").getValue().toString();
                                if (singleEmail.equals(email)){
                                    Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                                    present = true;
                                    break;
                                }
                            }

                            if (!present) {
                                Intent intent = new Intent(RegisterActivity.this, OtpActivity.class);
                                intent.putExtra("EMAIL", email);
                                intent.putExtra("PASSWORD", password);
                                startActivity(intent);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });
    }
        private void ShowError(String error) {
            Toast.makeText(RegisterActivity.this, "Please, Enter a valid "+error, Toast.LENGTH_LONG).show();
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