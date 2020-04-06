package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private TextView tvProfileName,tvProfileStatus,tvFriendsCount;
    private Button btRequest,btDecline;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationdatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private String current_state;
    private FirebaseUser mCurrent_user;
    private  String display_name,status,image;
    private Toolbar toolbar;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.profile_toolbar);
        vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("User Profile");


        final String user_id=getIntent().getStringExtra("user_id");

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationdatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrent_user.getUid());



        ivProfilePhoto=findViewById(R.id.ivProfilePhoto);
        tvProfileName=findViewById(R.id.tvProfileName);
        tvProfileStatus=findViewById(R.id.tvProfileStatus);
        btRequest=findViewById(R.id.btRequest);
        tvFriendsCount = findViewById(R.id.tvFriendsCount);
        btDecline=findViewById(R.id.btDecline);
        tvFriendsCount=findViewById(R.id.tvFriendsCount);

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        mFriendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long count = (long) dataSnapshot.getChildrenCount();

                tvFriendsCount.setText("Friends Count : "+count);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                display_name=dataSnapshot.child("name").getValue().toString();
                status=dataSnapshot.child("status").getValue().toString();
                image=dataSnapshot.child("image").getValue().toString();

                current_state="not_friends";

                tvProfileName.setText(display_name);
                tvProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(ivProfilePhoto);

                //------------------Friend List-Button change

                mFriendReqDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")){
                                current_state="req_received";
                                btRequest.setText("Accept Friend Request");

                                btDecline.setEnabled(true);
                                btDecline.setVisibility(View.VISIBLE);
                            }
                            else {
                                current_state="req_sent";
                                btRequest.setText("Cancel Friend Request");
                            }
                            progressDialog.dismiss();

                        }
                        else {

                            mFriendDatabase.child(mCurrent_user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)){

                                        current_state="friends";
                                        btRequest.setText("UnFriend "+display_name);
                                        btDecline.setEnabled(false);
                                        btDecline.setVisibility(View.INVISIBLE);

                                    }
                                    progressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });

                        }


                        progressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });


        if (user_id.equals(mCurrent_user.getUid())){
            btRequest.setEnabled(false);
            btRequest.setVisibility(View.INVISIBLE);
        }

        btRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btRequest.setEnabled(false);

                vibrator.vibrate(100);



//---------------------Not FRIENDS STATE


                    if (current_state.equals("not_friends")) {

                        DatabaseReference newNotification = mRootRef.child("notification").child(user_id).push();
                        String newNotificationId = newNotification.getKey();

                        HashMap<String, String> notificationdata = new HashMap<>();
                        notificationdata.put("from", mCurrent_user.getUid());
                        notificationdata.put("type", "request");

                        Map requestMap = new HashMap();
                        requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                        requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                        requestMap.put("notification/" + user_id + "/" + newNotificationId, notificationdata);

                        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {


                                btRequest.setEnabled(true);
                                current_state = "req_sent";
                                btRequest.setText("Cancel Friend Request");


                            }
                        });

                    }

                    //......Cancel Friend Request

                    if (current_state.equals("req_sent")) {

                        mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            btRequest.setEnabled(true);
                                            current_state = "not_friends";
                                            btRequest.setText("Send Friend Request");

                                        }
                                    });
                                }
                            }


                        });

                    }

                    //.......................Request Received State

                    if (current_state.equals("req_received")) {

                        final String current_date = DateFormat.getDateTimeInstance().format(new Date());

                        Map friendsMap = new HashMap();
                        friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", current_date);
                        friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", current_date);
                        friendsMap.put("Friend_req" + mCurrent_user.getUid() + "/" + user_id, null);
                        friendsMap.put("Friend_req" + "/" + user_id + mCurrent_user.getUid(), null);

                        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                if (databaseError == null) {
                                    btRequest.setEnabled(true);
                                    current_state = "friends";
                                    btRequest.setText("Unfriend this Person");

                                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        }


                                    });


                                } else {
                                    String error = databaseError.getMessage();
                                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }

                    //.................UnFriend

                    if (current_state.equals("friends")) {

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked

                                        mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {


                                                            mRootRef.child("Chat").child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        mRootRef.child("Chat").child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {

                                                                                btRequest.setEnabled(true);
                                                                                current_state = "not_friends";
                                                                                btRequest.setText("Send Friend Request");
                                                                                btDecline.setVisibility(View.INVISIBLE);




                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            });


                                                        }
                                                    });
                                                }
                                            }


                                        });


                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setMessage("Are you sure ?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();


                    }


            }
        });

        btDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vibrator.vibrate(100);

                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btRequest.setEnabled(true);
                                    current_state="not_friends";
                                    btRequest.setText("Send Friend Request");

                                }
                            });
                        }
                    }


                });
            }
        });

    }
}
