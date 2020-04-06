package com.example.chattingapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.VIBRATOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFrag extends Fragment {

    private RecyclerView requestList;

    private DatabaseReference mFriendReqDatabase, Friend_req;
    private  DatabaseReference mUsersDatabase,mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private FirebaseUser mCurrent_user;
    private  View mMainView;
private Button btRequest;
private Vibrator vibrator;
private TextView tvEmpty;
private ImageView ivEmpty;



    public RequestsFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView =inflater.inflate(R.layout.fragment_requests, container, false);

        requestList=(RecyclerView) mMainView.findViewById(R.id.request_list);
        vibrator= (Vibrator) getActivity().getSystemService(VIBRATOR_SERVICE);

        tvEmpty=mMainView.findViewById(R.id.no_request_tv);
        ivEmpty=mMainView.findViewById(R.id.no_request_img);

        mAuth= FirebaseAuth.getInstance();


if (mAuth.getCurrentUser() != null) {
    mCurrent_user_id = mAuth.getCurrentUser().getUid();


    mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
    Friend_req = FirebaseDatabase.getInstance().getReference().child("Friend_req");
    mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();
    mRootRef = FirebaseDatabase.getInstance().getReference();


    mFriendReqDatabase.keepSynced(true);
    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    mUsersDatabase.keepSynced(true);
}



        requestList.setLayoutManager(new LinearLayoutManager(getContext()));


        return  mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();


            mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Friend_req")){

                    Friend_req.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mCurrent_user_id)) {


                                FirebaseRecyclerAdapter<Request, requestViewHolder> firebaseRecyclerAdapter;
                                firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Request, requestViewHolder>(new FirebaseRecyclerOptions.Builder<Request>().setQuery(mFriendReqDatabase, Request.class).build()) {
                                    @NonNull
                                    @Override
                                    public requestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_layout, parent, false);
                                        RequestsFrag.requestViewHolder FriendsViewHolder = new RequestsFrag.requestViewHolder(view);
                                        return FriendsViewHolder;
                                    }

                                    @Override
                                    protected void onBindViewHolder(@NonNull final requestViewHolder holder, int position, @NonNull Request model) {


                                        final String user_id = getRef(position).getKey();



                                        if (model.getRequest_type().equals("received")) {


                                            mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    String name = dataSnapshot.child("name").getValue().toString();
                                                    String status = dataSnapshot.child("status").getValue().toString();
                                                    String image = dataSnapshot.child("thumb_image").getValue().toString();


                                                    holder.user_name.setText(name);
                                                    Picasso.with(getContext()).load(image).placeholder(R.drawable.default_avatar).into(holder.user_image);

                                                }


                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                        else {
                                            holder.itemView.setVisibility(View.INVISIBLE);
                                        }

                                        holder.btAccept.setOnClickListener(new View.OnClickListener() {


                                            @Override
                                            public void onClick(View v) {

                                                vibrator.vibrate(100);
                                                final String current_date = DateFormat.getDateTimeInstance().format(new Date());

                                                Map friendsMap = new HashMap();
                                                friendsMap.put("Friends/" + mCurrent_user_id + "/" + user_id + "/date", current_date);
                                                friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", current_date);
                                                friendsMap.put("Friend_req" + mCurrent_user.getUid() + "/" + user_id, null);
                                                friendsMap.put("Friend_req" + "/" + user_id + mCurrent_user.getUid(), null);

                                                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                                        if (databaseError == null) {


                                                            Friend_req.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {
                                                                        Friend_req.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                holder.itemView.setVisibility(View.INVISIBLE);
                                                                            }
                                                                        });
                                                                    }
                                                                }


                                                            });


                                                        } else {
                                                            String error = databaseError.getMessage();
                                                            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            }
                                        });

                                        holder.btReject.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                vibrator.vibrate(100);

                                                Friend_req.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            Friend_req.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {


                                                                }
                                                            });
                                                        }
                                                    }


                                                });
                                            }
                                        });


                                    }


                                };


                                requestList.setAdapter(firebaseRecyclerAdapter);

                                firebaseRecyclerAdapter.startListening();

                                tvEmpty.setVisibility(View.GONE);
                                ivEmpty.setVisibility(View.GONE);



                            }else
                            {
                                tvEmpty.setVisibility(View.VISIBLE);
                                ivEmpty.setVisibility(View.VISIBLE);
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                }else
                {
                    tvEmpty.setVisibility(View.VISIBLE);
                    ivEmpty.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }
    public static class requestViewHolder extends RecyclerView.ViewHolder{

        TextView user_name,user_status;
        ImageView btAccept, btReject;
        CircleImageView user_image;

        public requestViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.request_profile_image);
            user_name = itemView.findViewById(R.id.request_user_name);
            user_status = itemView.findViewById(R.id.request_user_status);
            btAccept = itemView.findViewById(R.id.btAccept);
            btReject = itemView.findViewById(R.id.btReject);

        }
    }
}
