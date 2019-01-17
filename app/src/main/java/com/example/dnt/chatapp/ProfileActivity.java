package com.example.dnt.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID, currentID, currentState;
    private CircleImageView profileAvatar;
    private TextView profileUserName, profileStatus;
    private Button profileSendMsgRequest, profileCancelRequest;
    private DatabaseReference userRef, chatRef, contactRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        receiverUserID = getIntent().getExtras().getString("userID");
        profileAvatar = findViewById(R.id.profile_avatar);
        profileUserName = findViewById(R.id.profile_user_name);
        profileStatus = findViewById(R.id.profile_user_status);
        profileSendMsgRequest = findViewById(R.id.profile_send_msg);
        profileCancelRequest = findViewById(R.id.profile_cancel_request);
        currentState = "new";
        mAuth = FirebaseAuth.getInstance();
        currentID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        RetrieveUserInfo();

    }

    private void RetrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))) {
                    String avatar = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(avatar).into(profileAvatar);
                    profileUserName.setText(userName);
                    profileStatus.setText(userStatus);

                    ChatRequestMgr();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    profileUserName.setText(userName);
                    profileStatus.setText(userStatus);
                    ChatRequestMgr();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void ChatRequestMgr() {
        chatRef.child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)) {
                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        currentState = "request_send";
                        profileSendMsgRequest.setText("Cancel");
                    }
                    if (request_type.equals("received")) {
                        profileCancelRequest.setVisibility(View.VISIBLE);
                        profileCancelRequest.setEnabled(true);
                        currentState = "request_received";
                        profileSendMsgRequest.setText("Accept");

                        profileCancelRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelRequest();
                            }
                        });
                    }
                } else {
                    contactRef.child(currentID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)) {
                                currentState = "friend";
                                profileSendMsgRequest.setText("Unfriend");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (currentID.equals(receiverUserID)) {
            profileSendMsgRequest.setVisibility(View.INVISIBLE);
        } else {
            profileSendMsgRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profileSendMsgRequest.getText().toString().equals("Cancel")){
                        CancelRequest();
                    }
                    if (currentState.equals("new")) {
                        SendChatRequest();
                    }
                    if (currentState.equals("request_sent")) {
                        CancelRequest();
                    }
                    if (currentState.equals("request_received")) {
                        AcceptRequest();
                    }
                    if (currentState.equals("friend")) {
                        Unfriend();
                    }
                }
            });
        }
    }

    private void SendChatRequest() {
        chatRef.child(currentID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRef.child(receiverUserID).child(currentID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                profileSendMsgRequest.setEnabled(true);
                                                currentState = "request_sent";
                                                profileSendMsgRequest.setText("Cancel");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelRequest() {
        chatRef.child(currentID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRef.child(receiverUserID).child(currentID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                profileSendMsgRequest.setEnabled(true);
                                                currentState = "new";
                                                profileSendMsgRequest.setText("Send Message");
                                                profileCancelRequest.setVisibility(View.INVISIBLE);
                                                profileCancelRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptRequest() {
        contactRef.child(currentID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiverUserID).child(currentID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                chatRef.child(currentID).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            chatRef.child(receiverUserID).child(currentID)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    profileSendMsgRequest.setEnabled(true);
                                                                    currentState = "friend";
                                                                    profileSendMsgRequest.setText("Unfriend");
                                                                    profileCancelRequest.setVisibility(View.INVISIBLE);
                                                                    profileCancelRequest.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
    private void Unfriend() {
        contactRef.child(currentID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactRef.child(receiverUserID).child(currentID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                profileSendMsgRequest.setEnabled(true);
                                                currentState = "new";
                                                profileSendMsgRequest.setText("Send Message");
                                                profileCancelRequest.setVisibility(View.INVISIBLE);
                                                profileCancelRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

}