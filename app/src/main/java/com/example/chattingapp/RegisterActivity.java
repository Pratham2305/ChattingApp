package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText inNAME,inEMAIL,inPASSWORD;
    private Button btSUBMIT;
    private FirebaseAuth mAuth;
    private ProgressDialog RegProgress;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        inEMAIL=findViewById(R.id.inEMAIL);
        inNAME=findViewById(R.id.inNAME);
        inPASSWORD=findViewById(R.id.inPASSWORD);
        btSUBMIT=findViewById(R.id.btSUBMIT);
        RegProgress =new ProgressDialog(this);

        btSUBMIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getName=inNAME.getEditableText().toString();
                String getEmail=inEMAIL.getEditableText().toString();
                String getPassword=inPASSWORD.getEditableText().toString();

                if (TextUtils.isEmpty(getName) || TextUtils.isEmpty(getEmail) || TextUtils.isEmpty(getPassword)){
                    Toast.makeText(RegisterActivity.this,"Please Enter All Fields",Toast.LENGTH_LONG).show();
                }
                else {
                    RegProgress.setTitle("Registering User");
                    RegProgress.setMessage("Please Wait");
                    RegProgress.setCanceledOnTouchOutside(false);
                    RegProgress.show();
                    register_user(getName, getEmail, getPassword);
                }

            }
        });
    }

    private void register_user(final String getName, String getEmail, String getPassword) {

        mAuth.createUserWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                    String user_id= current_user.getUid();
                    myRef=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                    String deviceToken= FirebaseInstanceId.getInstance().getToken();


                    HashMap<String,String> userMap= new HashMap<>();
                    userMap.put("name",getName);
                    userMap.put("status","Hi there Im using This Chatting App");
                    userMap.put("image","default");
                    userMap.put("thumb_image","default");
                    userMap.put("device_token",deviceToken);

                    myRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                RegProgress.dismiss();
                                Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });


                   /* */
                }
                else{
                    RegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"ERROR Ha ha XD",Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
