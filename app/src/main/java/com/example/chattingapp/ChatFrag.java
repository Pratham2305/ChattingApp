package com.example.chattingapp;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFrag extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase,mChatDatabase;
    private DatabaseReference mLastMessage, mFriendDatabase;
    private DatabaseReference mCheckDatabase;

    private FirebaseAuth mAuth;
    private String date;

    private String mCurrent_user_id;
    private View mMainView;
     String name,image;
     private TextView no_chat_tv;
     private ImageView no_chat_img;

    public ChatFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);

        mConvList=(RecyclerView) mMainView.findViewById(R.id.conversation_list);
        mAuth= FirebaseAuth.getInstance();
        no_chat_tv=mMainView.findViewById(R.id.no_chat_tv);
        no_chat_img=mMainView.findViewById(R.id.no_chat_img);


            mCurrent_user_id = mAuth.getCurrentUser().getUid();

            mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mLastMessage = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
            mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends");


            if (FirebaseDatabase.getInstance().getReference().child("Chat")!=null) {


                mCheckDatabase= FirebaseDatabase.getInstance().getReference().child("Chat");

                mCheckDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        if (dataSnapshot.hasChild(mCurrent_user_id)) {
                            mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            mMessageDatabase.keepSynced(true);
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);


        mConvList.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return mMainView;


    }
    @Override
    public void onStart() {
        super.onStart();


        mCheckDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mCurrent_user_id)){

                    Query convQuery = mChatDatabase.orderByChild("timestamp");

                    FirebaseRecyclerAdapter<Conv,ConvViewHolder> firebaseRecyclerAdapter= new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(new FirebaseRecyclerOptions.Builder<Conv>().setQuery(convQuery,Conv.class).build()) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {

                            final  String list_user_id = getRef(position).getKey();

                            mFriendDatabase.child(mCurrent_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(list_user_id)){

                                        Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                            String data = dataSnapshot.child("message").getValue().toString();
                                            String type = dataSnapshot.child("type").getValue().toString();

                                            if (type.equals("text")) {
                                                holder.userStatus.setText(data);
                                            } else if (type.equals("image")) {
                                                holder.userStatus.setText("image");
                                            }

                                            if (!model.isSeen()) {
                                                holder.userStatus.setTypeface(holder.userStatus.getTypeface(), Typeface.BOLD);
                                            } else {
                                                holder.userStatus.setTypeface(holder.userStatus.getTypeface(), Typeface.NORMAL);
                                            }
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

                                    mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            name = dataSnapshot.child("name").getValue().toString();
                                            image = dataSnapshot.child("thumb_image").getValue().toString();

                                            if (dataSnapshot.hasChild("online")) {
                                                String userOnline = dataSnapshot.child("online").getValue().toString();

                                                if (userOnline.equals("true")) {
                                                    holder.user_online_icon.setVisibility(View.VISIBLE);
                                                } else
                                                    holder.user_online_icon.setVisibility(View.INVISIBLE);
                                            }

                                            holder.userName.setText(name);
                                            Picasso.with(getContext()).load(image).placeholder(R.drawable.default_avatar).into(holder.userImg);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                                    intent.putExtra("user_id", list_user_id);


                                                    intent.putExtra("user_name", dataSnapshot.child("name").getValue().toString());
                                                    intent.putExtra("image_url", dataSnapshot.child("thumb_image").getValue().toString());


                                                    startActivity(intent);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });


                                        }
                                    });
                                }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });



                        }



                        @NonNull
                        @Override
                        public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_layout,parent,false);
                            ChatFrag.ConvViewHolder ConvViewHolder = new ChatFrag.ConvViewHolder(view);
                            return ConvViewHolder;
                        }
                    };


                        no_chat_img.setVisibility(View.GONE);
                        no_chat_tv.setVisibility(View.GONE);


                    mConvList.setAdapter(firebaseRecyclerAdapter);
                    firebaseRecyclerAdapter.startListening();


                }
                else {
                    no_chat_img.setVisibility(View.VISIBLE);
                    no_chat_tv.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView userImg;
        ImageView user_online_icon;


        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);


            userImg=itemView.findViewById(R.id.chat_profile_image);
            userName=itemView.findViewById(R.id.chat_name);
            userStatus=itemView.findViewById(R.id.chat_status);
            user_online_icon=itemView.findViewById(R.id.chat_online_icon);

        }
    }



}
