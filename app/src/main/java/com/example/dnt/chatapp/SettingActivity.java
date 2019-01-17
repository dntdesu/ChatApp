package com.example.dnt.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private Button Update;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int galleryPick = 1;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Setting");
        InitField();
        userName.setVisibility(View.INVISIBLE);
        RetrieveUserInfo();
        Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });
    }

    private void InitField() {
        Update = findViewById(R.id.updateSettingButton);
        userName = findViewById(R.id.set_userName);
        userStatus = findViewById(R.id.set_profileStatus);
        userProfileImage = findViewById(R.id.set_profile_image);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Image");
        loadingBar = new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            loadingBar.setTitle("Setting avatar");
            loadingBar.setMessage("Please wait a moment");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            final Uri resultUri = result.getUri();
            final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
            filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    });
                }
            });
        }
    }


    private void UpdateSetting() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();
        if (setUserName.isEmpty()) {
            Toast.makeText(this, "Enter the your name please", Toast.LENGTH_SHORT).show();
        } else {
            final HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setUserStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                String msg = task.getException().toString();
                                int index = msg.indexOf("Exception") + 11;
                                Toast.makeText(SettingActivity.this, "Error : " + msg.substring(index), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);

                        } else if (dataSnapshot.hasChild("image")) {
                            userName.setVisibility(View.VISIBLE);
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        } else {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this, "Update your profile for the first time please!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
