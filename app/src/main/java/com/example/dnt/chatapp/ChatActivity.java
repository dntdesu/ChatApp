package com.example.dnt.chatapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String receiverID, receiverName, receiverAvatar, messageSenderID;
    private TextView name, lastSeen;
    private CircleImageView avatar;
    private Toolbar chatToolbar;
    private ImageButton sendButton;
    private EditText message;
    private FirebaseAuth auth;
    private DatabaseReference RootRef;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        auth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        messageSenderID = auth.getCurrentUser().getUid();
        receiverID = getIntent().getExtras().get("ID").toString();
        receiverName = getIntent().getExtras().get("name").toString();
        receiverAvatar = getIntent().getExtras().get("avatar").toString();
        InitFields();

        name.setText(receiverName);
        Picasso.get().load(receiverAvatar).placeholder(R.drawable.profile_image).into(avatar);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RootRef.child("Message").child(messageSenderID).child(receiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message message = dataSnapshot.getValue(Message.class);

                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void InitFields() {
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        name = findViewById(R.id.custom_name);
        lastSeen = findViewById(R.id.custom_lastSeen);
        avatar = findViewById(R.id.custom_avatar);
        sendButton = findViewById(R.id.chat_sendButton);
        message = findViewById(R.id.chat_msgText);
        messageAdapter = new MessageAdapter(messageList);
        userMessagesList = findViewById(R.id.chat_list_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);
    }

    private void SendMessage() {
        String msg = message.getText().toString();
        if (!TextUtils.isEmpty(msg)){
            String messageSenderRef = "Message/" + messageSenderID + "/" + receiverID;
            String messageReceiverRef = "Message/" + receiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Message").child(messageSenderID).child(receiverID).push();
            String messagePushID = userMessageKeyRef.getKey();
            Map MsgBody = new HashMap();
            MsgBody.put("message", msg);
            MsgBody.put("type", "text");
            MsgBody.put("from", messageSenderID);

            Map MsgDetails = new HashMap();
            MsgDetails.put(messageSenderRef + "/" + messagePushID, MsgBody);
            MsgDetails.put(messageReceiverRef + "/" + messagePushID, MsgBody);
            RootRef.updateChildren(MsgDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    message.setText("");
                }
            });

        }
    }

}
