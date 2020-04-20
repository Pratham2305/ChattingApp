package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import  androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.chattingapp.GetTimeAgo.getTimeAgo;

public class ChatActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;

    private String mChatUser,userName,imageUrl;
    private Toolbar toolbar;
    private DatabaseReference mUserDatabase;
    private  DatabaseReference mRootRef;
    private DatabaseReference mMessageDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;
    private CircleImageView chat_user_image;
    private TextView last_seen,user_name;
    private ImageButton btChatSent, btChatAdd;
    private EditText etChatMessage;
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearlayout;
    private MessageAdapter mAdapter;
    private static  final  int TOTAL_TIMES_TO_LOAD = 10;
    private int mCurrentPage=1;
    private SwipeRefreshLayout mRefreshLayout;

    private int itemPos = 0;
    private String mLastKey="";
    private String mPrevKey;
    private Vibrator vibrator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatUser = getIntent().getStringExtra("user_id");
        userName=getIntent().getStringExtra("user_name");
        imageUrl=getIntent().getStringExtra("image_url");
        vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser);
        mRootRef= FirebaseDatabase.getInstance().getReference();
        mImageStorage=  FirebaseStorage.getInstance().getReference();
        mAuth= FirebaseAuth.getInstance();
        mCurrentUserID=mAuth.getCurrentUser().getUid();

        toolbar=findViewById(R.id.chat_toolbar);
        etChatMessage=findViewById(R.id.etChatMessage);
        btChatAdd=findViewById(R.id.btChatAdd);
        btChatSent=findViewById(R.id.btChatSent);
        mRefreshLayout=findViewById(R.id.message_swipe_layout);


        mAdapter= new MessageAdapter(messagesList);
        mMessagesList= findViewById(R.id.messages_list);
        mLinearlayout= new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearlayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();

        final LayoutInflater inflater= (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_chat_bar,null);

        actionBar.setCustomView(action_bar_view);


        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

       chat_user_image=findViewById(R.id.chat_toolbar_img);
       last_seen=findViewById(R.id.chat_toolbar_status);
       user_name=findViewById(R.id.chat_toolbar_name);

       user_name.setText(userName);


       toolbar.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {



               DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       switch (which){
                           case DialogInterface.BUTTON_POSITIVE:
                               //Yes button clicked
                               Intent intent=new Intent(ChatActivity.this,ProfileActivity.class);
                               intent.putExtra("user_id",mChatUser);
                               startActivity(intent);

                               break;

                           case DialogInterface.BUTTON_NEGATIVE:
                               //No button clicked
                               break;
                       }
                   }
               };

               AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
               builder.setMessage("Want to Open User's Profile?").setPositiveButton("Yes", dialogClickListener)
                       .setNegativeButton("No", dialogClickListener).show();


           }
       });



        Picasso.with(ChatActivity.this).load(imageUrl).placeholder(R.drawable.default_avatar).into(chat_user_image);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();

                if (online.equals("true")){
                    last_seen.setText("Online");
                }
                else {

                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    long lastTime= Long.parseLong(online);
                    String lastSeenTime = getTimeAgo(lastTime,getApplicationContext());
                    last_seen.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



            mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(mChatUser)) {
                        Map chatAddMap = new HashMap();
                        chatAddMap.put("seen", false);
                        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                        Map chatuserMap = new HashMap();
                        chatuserMap.put("Chat/" + mCurrentUserID + "/" + mChatUser, chatAddMap);
                        chatuserMap.put("Chat/" + mChatUser + "/" + mCurrentUserID, chatAddMap);

                        mRootRef.updateChildren(chatuserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                if (databaseError != null) {

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



        btChatSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                vibrator.vibrate(50);
                sendMessage();


            }

        });

        btChatAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"SELECT_IMAGE"),GALLERY_PICK);
            }
        });










    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageuri=data.getData();

            final String current_user_ref = "messages/"+mCurrentUserID+"/"+mChatUser;
            final String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserID;

            final DatabaseReference message_push = mRootRef.child("messages").child(mCurrentUserID).child(mChatUser).push();
            final String push_id = message_push.getKey();

            final StorageReference filepath = mImageStorage.child("message_images").child(push_id+".jpg");

            filepath.putFile(imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String download_url = uri.toString();

                                Map messageMap = new HashMap();
                                messageMap.put("message",download_url);
                                messageMap.put("seen",false);
                                messageMap.put("type","image");
                                messageMap.put("time",ServerValue.TIMESTAMP);
                                messageMap.put("from",mCurrentUserID);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
                                messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

                                etChatMessage.setText("");

                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                        if (databaseError != null){



                                        }

                                    }
                                });


                            }
                        });

                    }

                }
            });

        }


    }

    private void loadMoreMessages() {
        DatabaseReference messageRef= mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

                messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages= dataSnapshot.getValue(Messages.class);
                String messageKey=dataSnapshot.getKey();


                if (!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,messages);
                }else {
                    mPrevKey=mLastKey;

                }

                if (itemPos==1){
                    mLastKey = messageKey;
                }


                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);

                mLinearlayout.scrollToPositionWithOffset(10,0);

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



    private void loadMessages() {

        DatabaseReference messageRef= mRootRef.child("messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_TIMES_TO_LOAD);

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages= dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                itemPos++;
                if (itemPos==1){
                    mLastKey = dataSnapshot.getKey();
                    mPrevKey=mLastKey;
                }
                    mAdapter.notifyDataSetChanged();

                    mMessagesList.scrollToPosition(messagesList.size()-1);
                mRefreshLayout.setRefreshing(false);



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


    private void sendMessage() {

        String message =  etChatMessage.getText().toString();
        etChatMessage.getText().clear();

        if (!TextUtils.isEmpty(message)){

            String current_user_ref ="messages/"+mCurrentUserID+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserID;

            DatabaseReference user_message_ref= mRootRef.child("messages").child(mCurrentUserID).child(mChatUser).push();
            String push_id = user_message_ref.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref+"/"+push_id,messageMap);
            messageUserMap.put(chat_user_ref+"/"+push_id,messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }

                }
            });

        }

    }

}
