package com.example.dnt.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> userMessageList;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Message> userMessageList) {
        this.userMessageList = userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessage, receiverMessage;
        CircleImageView receiverAvatar;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = itemView.findViewById(R.id.custom_message_sender);
            receiverMessage = itemView.findViewById(R.id.custom_message_receiver);
            receiverAvatar = itemView.findViewById(R.id.custom_message_avatar);

        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_message_layout, viewGroup, false);
        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String senderID = auth.getCurrentUser().getUid();
        Message message = userMessageList.get(i);

        String fromUserID = message.getFrom();
        String messageType = message.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverAvatar = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverAvatar).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverAvatar);

                }
            }

            ;

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (messageType.equals("text")) {
            messageViewHolder.receiverMessage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverAvatar.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessage.setVisibility(View.INVISIBLE);
            if (fromUserID.equals(senderID)) {
                messageViewHolder.senderMessage.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessage.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessage.setText(message.getMessage());
            } else {

                messageViewHolder.receiverMessage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverAvatar.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessage.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.receiverMessage.setText(message.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


}
