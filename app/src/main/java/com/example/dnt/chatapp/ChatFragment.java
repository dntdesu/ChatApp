package com.example.dnt.chatapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private View chatView;
    private RecyclerView chatList;
    private DatabaseReference chatRef, userRef;
    private FirebaseAuth auth;
    private String currentID;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        chatView = inflater.inflate(R.layout.fragment_chat, container, false);
        chatList = chatView.findViewById(R.id.chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));
        auth = FirebaseAuth.getInstance();
        currentID = auth.getCurrentUser().getUid();
        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return chatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contacts model) {
                        final String userID = getRef(position).getKey();

                        userRef.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("image")){
                                    final String avatar = dataSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(avatar).placeholder(R.drawable.profile_image).into(holder.avatar);
                                }

                                final String name = dataSnapshot.child("name").getValue().toString();
                                final String status = dataSnapshot.child("status").getValue().toString();
                                holder.name.setText(name);
                                holder.status.setText("Last seen : ");


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        return  new ChatViewHolder(view);
                    }
                };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        CircleImageView avatar;
        TextView name, status;


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.user_avatar);
            name = itemView.findViewById(R.id.user_name);
            status = itemView.findViewById(R.id.user_status);
        }
    }
}
