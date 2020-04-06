package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser current_user;
    private CircleImageView circleImageView;
    private TextView tvDisplayName,tvStatus;
    private Button btChangeStatus,btChangeImage;
    private final int GALLERY_PICK=1;
    String status,name,image,thumb_image;
    private StorageReference mStorageRef;
    private ProgressDialog RegProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=current_user.getUid();

        tvDisplayName=findViewById(R.id.tvDisplayName);
        tvStatus=findViewById(R.id.tvStatus);
        circleImageView=findViewById(R.id.circleImageView);
        btChangeImage=findViewById(R.id.btChangeImage);
        btChangeStatus=findViewById(R.id.btChangeStatus);


        mStorageRef = FirebaseStorage.getInstance().getReference();


        btChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value=tvStatus.getText().toString();
                Intent intent=new Intent(SettingsActivity.this,StatusActivity.class);
                intent.putExtra("Status",status_value);
                startActivity(intent);
            }
        });

        btChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery_intent=new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });


        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name=dataSnapshot.child("name").getValue().toString();
                status=dataSnapshot.child("status").getValue().toString();
                image=dataSnapshot.child("image").getValue().toString();
                thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                tvDisplayName.setText(name);
                tvStatus.setText(status);

                if (!thumb_image.equals("default")) {

                    Picasso.with(SettingsActivity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(circleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(thumb_image).placeholder(R.drawable.default_avatar).into(circleImageView);
                        }
                    });
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    @Override
    protected  void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageuri=data.getData();
            CropImage.activity(imageuri)
                    .setAspectRatio(1,1)
                     .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                RegProgress =new ProgressDialog(this);
                RegProgress.setTitle("Uploading Image...");
                RegProgress.setMessage("Please Wait");
                RegProgress.setCanceledOnTouchOutside(false);
                RegProgress.show();

                Uri resultUri = result.getUri();

                File thumb_filepath= new File(resultUri.getPath());

                String current_userid =current_user.getUid();

                Bitmap thumb_bitmap=new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filepath);

                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumb_byte=baos.toByteArray();

                final StorageReference filepath=mStorageRef.child("profile_images").child( current_userid+".jpg");
                final StorageReference thump_filepath=mStorageRef.child("profile_images").child("thumbs").child(current_userid+".jpg");


                filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {


                                final String profileImageUrl=task.getResult().toString();

                                UploadTask uploadTask=thump_filepath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()){
                                            thump_filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> thumb_task) {

                                                    String thumb_downloadUrl=thumb_task.getResult().toString();

                                                    if (thumb_task.isSuccessful()){

                                                        Map update_Map=new HashMap();
                                                        update_Map.put("image",profileImageUrl);
                                                        update_Map.put("thumb_image",thumb_downloadUrl);

                                                        databaseReference.updateChildren(update_Map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    RegProgress.dismiss();
                                                                }
                                                            }
                                                        });

                                                    }else {Toast.makeText(SettingsActivity.this,"Error Uploading",Toast.LENGTH_LONG).show();
                                                        RegProgress.dismiss();}
                                                }
                                            });
                                        }

                                    }
                                });


                            }
                        });
                    }
                });





            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
