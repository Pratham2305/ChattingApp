package com.example.chattingapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
public class FriendsFrag extends Fragment {

    private RecyclerView friendList;
    private DatabaseReference mFriendDatabase;
    private  DatabaseReference mUsersDatabase;
    private DatabaseReference mCheckDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private  View mMainView;
    private TextView tvEmpty;
    private ImageView ivEmpty;



    public FriendsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         mMainView =inflater.inflate(R.layout.fragment_friends, container, false);

        friendList=(RecyclerView) mMainView.findViewById(R.id.friendsList);
        tvEmpty=mMainView.findViewById(R.id.no_friend_tv);
        ivEmpty=mMainView.findViewById(R.id.no_friend_iv);
        mAuth= FirebaseAuth.getInstance();

        mCurrent_user_id=mAuth.getCurrentUser().getUid();

        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mCheckDatabase= FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);


        friendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        mCheckDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mCurrent_user_id)){


                    FirebaseRecyclerOptions<Friends> options =

                            new FirebaseRecyclerOptions.Builder<Friends>()
                                    .setQuery( mFriendDatabase, Friends.class)
                                    .build();

                    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecylcerViewHolder = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
                        @NonNull
                        @Override
                        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_layout,parent,false);
                            FriendsFrag.FriendsViewHolder FriendsViewHolder = new FriendsFrag.FriendsViewHolder(view);
                            return FriendsViewHolder;
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {

                            holder.date.setText(model.getDate());

                            final String list_user_id=getRef(position).getKey();

                            mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    final String userName = dataSnapshot.child("name").getValue().toString();
                                    String userStatus= dataSnapshot.child("status").getValue().toString();
                                    final String imageUrl= dataSnapshot.child("thumb_image").getValue().toString();
                                    if (dataSnapshot.hasChild("online")){
                                        String online=  dataSnapshot.child("online").getValue().toString();

                                        if (online.equals("true")){

                                            holder.user_online_icon.setVisibility(View.VISIBLE);
                                        }

                                    }

                                    holder.userStatus.setText(userStatus);
                                    holder.userName.setText(userName);
                                    Picasso.with(getContext()).load(imageUrl).placeholder(R.drawable.default_avatar).into(holder.userImage);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            CharSequence options[]= new CharSequence[]{"Open Profile","Send Message"};
                                            AlertDialog.Builder builder= new AlertDialog.Builder(getContext());

                                            builder.setTitle("Select Options");
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    if (which==0){
                                                        Intent intent=new Intent(getContext(),ProfileActivity.class);
                                                        intent.putExtra("user_id",list_user_id);
                                                        startActivity(intent);
                                                    }
                                                    if (which == 1) {
                                                        Intent intent=new Intent(getContext(),ChatActivity.class);
                                                        intent.putExtra("user_id",list_user_id);
                                                        intent.putExtra("user_name",userName);
                                                        intent.putExtra("image_url",imageUrl);
                                                        startActivity(intent);

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



                        }

                    };




                        tvEmpty.setVisibility(View.GONE);
                        ivEmpty.setVisibility(View.GONE);




                    friendList.setAdapter(friendsRecylcerViewHolder);
                    friendsRecylcerViewHolder.startListening();


                }else {

                        tvEmpty.setVisibility(View.VISIBLE);
                        ivEmpty.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        TextView date,userName,userStatus;
        CircleImageView userImage;
            ImageView user_online_icon;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            date= itemView.findViewById(R.id.date);
            userImage=itemView.findViewById(R.id.friend_profile_image);
            userName=itemView.findViewById(R.id.friend_name);
            userStatus=itemView.findViewById(R.id.friend_status);
            user_online_icon=itemView.findViewById(R.id.user_online_icon);


        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
