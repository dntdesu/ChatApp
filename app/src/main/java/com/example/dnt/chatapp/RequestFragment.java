package com.example.dnt.chatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AlertDialogLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private View requestView;
    private RecyclerView request_view;
    private DatabaseReference chatRequestRef, userRef,contactRef;
    private FirebaseAuth auth;
    String currentID;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestView = inflater.inflate(R.layout.fragment_request, container, false);
        request_view = requestView.findViewById(R.id.request_view);
        request_view.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        auth = FirebaseAuth.getInstance();
        currentID = auth.getCurrentUser().getUid();

        return requestView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestRef.child(currentID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {
                        final String list_user = getRef(position).getKey();
                        DatabaseReference typeRef = getRef(position).child("request_type").getRef();
                        typeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received")) {
                                        userRef.child(list_user).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestUserAvatar = dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserAvatar).placeholder(R.drawable.profile_image).into(holder.avatar);
                                                }
                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                                holder.name.setText(requestUserName);
                                                holder.status.setText("Hi, I'm " + requestUserName + ". I want to be your friend");
                                                holder.status.setTextColor(Color.GREEN);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]{
                                                                "Accept",
                                                                "Cancel"
                                                        };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + " chat request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int i) {
                                                                if (i == 0){
                                                                    contactRef.child(currentID).child(list_user).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                contactRef.child(list_user).child(currentID).child("Contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            chatRequestRef.child(currentID).child(list_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful()){
                                                                                                        chatRequestRef.child(list_user).child(currentID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {

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
                                                                }else{
                                                                    chatRequestRef.child(currentID).child(list_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                chatRequestRef.child(list_user).child(currentID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        userRef.child(list_user).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestUserAvatar = dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserAvatar).placeholder(R.drawable.profile_image).into(holder.avatar);
                                                }
                                                final String requestUserName = dataSnapshot.child("name").getValue().toString();

                                                holder.name.setText(requestUserName);
                                                holder.status.setText("You sent a request");
                                                holder.status.setTextColor(Color.BLUE);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]{
                                                                "Cancel"
                                                        };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + " chat request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int i) {
                                                                    chatRequestRef.child(currentID).child(list_user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                chatRequestRef.child(list_user).child(currentID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });

                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        request_view.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView name, status;
        CircleImageView avatar;
        Button accept, deny;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.user_name);
            status = itemView.findViewById(R.id.user_status);
            avatar = itemView.findViewById(R.id.user_avatar);
        }
    }
}
