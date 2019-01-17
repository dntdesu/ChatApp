package com.example.dnt.chatapp;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.SimpleFormatter;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayMessages;
    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private FirebaseAuth auth;
    private DatabaseReference UserRef, GroupNameRef, GroupMsgRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        InitFields();
        GetUserInfo();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageToDatabase();
                userMessageInput.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
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
        toolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        displayMessages = findViewById(R.id.group_chat_text_display);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
    }

    private void GetUserInfo() {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageToDatabase() {
        String msg = userMessageInput.getText().toString();
        String msgKey = GroupNameRef.push().getKey();
        if (!TextUtils.isEmpty(msg)){
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
            currentDate = simpleDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm a");
            currentTime = simpleDateFormatTime.format(calendarTime.getTime());

            HashMap<String,Object> groupMsgKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMsgKey);
            GroupMsgRef = GroupNameRef.child(msgKey);

            HashMap<String,Object> msgInfoMap = new HashMap<>();
            msgInfoMap.put("name", currentUserName);
            msgInfoMap.put("message", msg);
            msgInfoMap.put("date", currentDate);
            msgInfoMap.put("time", currentTime);

            GroupMsgRef.updateChildren(msgInfoMap);
        }
    }

    private void DisplayMessage(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String message = (String) ((DataSnapshot)iterator.next()).getValue();
            String userName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayMessages.append(userName + " : " + message + System.getProperty("line.separator")
                    + chatTime + "    " + chatDate + System.getProperty("line.separator") + System.getProperty("line.separator"));
            scrollView.post(new Runnable() {
                public void run() {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

}
