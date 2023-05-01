package com.dripblood.myapplication.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dripblood.myapplication.R;
import com.dripblood.myapplication.viewmodels.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private EditText fullName, address, contact;
    private FirebaseAuth mAuth;
    private Button btnSignup;
    private ProgressDialog pd;
    private ProgressBar progressBar;
    private Spinner gender, bloodgroup;
    private CircleImageView imageView;
    private CheckBox isDonor;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    private boolean isUpdate = false;

    private DatabaseReference db_ref, donor_ref;
    private FirebaseDatabase db_User;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    private Uri uri;

    private String ema;
    private String pas;
    private String mob;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Log.i("STEPS", "Profile");

        Intent i = getIntent();
        ema = i.getStringExtra("EMAIL");
        pas = i.getStringExtra("PASSWORD");
        mob = i.getStringExtra("PHONE");

        pd = new ProgressDialog(this);
        pd.show();
        pd.setContentView(R.layout.progress_dialog);
        pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pd.dismiss();

        progressBar = findViewById(R.id.progBar);

        db_User = FirebaseDatabase.getInstance();
        db_ref = db_User.getReference("users");
        donor_ref = db_User.getReference("donors");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("ProfilePics");

        if (mAuth.getCurrentUser() != null) {
            id = mAuth.getCurrentUser().getUid();
        }

        fullName = findViewById(R.id.input_fullName);
        gender = findViewById(R.id.gender);
        address = findViewById(R.id.inputAddress);
        bloodgroup = findViewById(R.id.inputBloodGroup);
        contact = findViewById(R.id.inputMobile);
        isDonor = findViewById(R.id.checkbox);
        btnSignup = findViewById(R.id.button_register);
        imageView = findViewById(R.id.profile_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(mob!=null){
            contact.setText(mob);
            contact.setEnabled(false);
        }else{
            contact.setEnabled(true);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        progressBar.setVisibility(View.GONE);
        pd.show();
        if (mAuth.getCurrentUser() != null) {
            btnSignup.setText("Update Profile");
            getSupportActionBar().setTitle("Profile");
            isUpdate = true;

            Query Profile = db_ref.child(mAuth.getCurrentUser().getUid());
            Profile.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    UserData userData = dataSnapshot.getValue(UserData.class);

                    if (userData != null) {

                        uri = mAuth.getCurrentUser().getPhotoUrl();

                        Picasso.get().load(uri).into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                imageView.setBackgroundResource(R.drawable.default_img);
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                        fullName.setText(userData.getName());
                        gender.setSelection(userData.getGender());
                        address.setText(userData.getAddress());
                        contact.setText(userData.getContact());
                        bloodgroup.setSelection(userData.getBloodGroup());

                        Query donor = donor_ref.child(bloodgroup.getSelectedItem().toString()).child(mAuth.getCurrentUser().getUid());

                        donor.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    isDonor.setChecked(true);
                                    isDonor.setText("Unmark this to leave from donors");
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Your are not a donor! Be a donor and save life by donating blood.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("User", databaseError.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("User", databaseError.getMessage());
                }
            });

        }
        pd.dismiss();

        btnSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                        if (fullName.getText().toString().length() < 2) {
                            ShowError("Name");
                            fullName.requestFocusFromTouch();
                        }else if (address.getText().toString().length() <= 2) {
                            ShowError("Address");
                            address.requestFocusFromTouch();
                        } else {
                            final String Name = fullName.getText().toString();
                            final int Gender = gender.getSelectedItemPosition();
                            final String Contact = contact.getText().toString();
                            final int BloodGroup = bloodgroup.getSelectedItemPosition();
                            final String Address = address.getText().toString();
                            final String blood = bloodgroup.getSelectedItem().toString();

                            if (!isUpdate) {
                                pd.show();
                                mAuth.createUserWithEmailAndPassword(ema, pas).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (!task.isSuccessful()) {
                                            pd.dismiss();
                                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            uploadPic();
                                            Toast.makeText(ProfileActivity.this, "", Toast.LENGTH_SHORT).show();
                                            id = mAuth.getCurrentUser().getUid();
                                            db_ref.child(id).child("Name").setValue(Name);
                                            db_ref.child(id).child("Email").setValue(ema);
                                            db_ref.child(id).child("Gender").setValue(Gender);
                                            db_ref.child(id).child("Contact").setValue(Contact);
                                            db_ref.child(id).child("BloodGroup").setValue(BloodGroup);
                                            db_ref.child(id).child("Address").setValue(Address);

                                            if (isDonor.isChecked()) {
                                                donor_ref.child(blood).child(id).child("UID").setValue(id).toString();
                                                donor_ref.child(blood).child(id).child("LastDonate").setValue("Haven't donate yet!");
                                                donor_ref.child(blood).child(id).child("TotalDonate").setValue(0);
                                                donor_ref.child(blood).child(id).child("Name").setValue(Name);
                                                donor_ref.child(blood).child(id).child("Email").setValue(ema);
                                                donor_ref.child(blood).child(id).child("Contact").setValue(Contact);
                                                donor_ref.child(blood).child(id).child("Address").setValue(Address);
                                            }
                                            pd.dismiss();
                                            Toast.makeText(getApplicationContext(), "Welcome, your account has been created!", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(ProfileActivity.this, Dashboard.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });

                            } else {
                                pd.show();
                                uploadPic();
                                db_ref.child(id).child("Name").setValue(Name);
                                db_ref.child(id).child("Email").setValue(ema);
                                db_ref.child(id).child("Gender").setValue(Gender);
                                db_ref.child(id).child("Contact").setValue(Contact);
                                db_ref.child(id).child("BloodGroup").setValue(BloodGroup);
                                db_ref.child(id).child("Address").setValue(Address);

                                if (isDonor.isChecked()) {
                                    donor_ref.child(blood).child(id).child("UID").setValue(id).toString();
                                    donor_ref.child(blood).child(id).child("LastDonate").setValue("Haven't donate yet!");
                                    donor_ref.child(blood).child(id).child("TotalDonate").setValue(0);
                                    donor_ref.child(blood).child(id).child("Name").setValue(Name);
                                    donor_ref.child(blood).child(id).child("Email").setValue(ema);
                                    donor_ref.child(blood).child(id).child("Contact").setValue(Contact);
                                    donor_ref.child(blood).child(id).child("Address").setValue(Address);
                                } else {
                                    donor_ref.child(blood).child(id).removeValue();
                                }
                                pd.dismiss();

                                Toast.makeText(getApplicationContext(), "Your account has been updated!", Toast.LENGTH_LONG).show();

                                startActivity(new Intent(ProfileActivity.this, Dashboard.class));
                                finish();
                            }
                        }
                }
            });
    }

    private void ShowError(String error) {
        Toast.makeText(ProfileActivity.this, "Please, Enter a valid "+error, Toast.LENGTH_LONG).show();
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            imageView.setImageURI(uriImage);
        }
    }

    private void uploadPic(){
        if(uriImage != null){
            StorageReference fileReference = storageReference.child(mAuth.getCurrentUser().getUid()+"."+getFileExtension(uriImage));

            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            Picasso.get().load(downloadUri).into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError(Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    });

                }
            });
        }
    };

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
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
