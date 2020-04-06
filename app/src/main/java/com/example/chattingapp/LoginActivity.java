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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button btLogin;
    private TextInputEditText inYourEmail,inPassword;
    private ProgressDialog RegProgress;
private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        btLogin=findViewById(R.id.btLogin);
        inYourEmail=findViewById(R.id.inYourEmail);
        inPassword=findViewById(R.id.inPassword);
        RegProgress =new ProgressDialog(this);


        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=inYourEmail.getEditableText().toString();
                String password=inPassword.getEditableText().toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    RegProgress.setTitle("Logging in");
                    RegProgress.setMessage("Please Wait");
                    RegProgress.setCanceledOnTouchOutside(false);
                    RegProgress.show();
                    loginUser(email,password);
                }
            }
        });


    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    RegProgress.dismiss();

                    String current_user_id= mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();

                    databaseReference.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });


                }else {
                    RegProgress.hide();
                    Toast.makeText(LoginActivity.this,"Wrong Input",Toast.LENGTH_LONG).show();

                }

            }
        });
    }
}
