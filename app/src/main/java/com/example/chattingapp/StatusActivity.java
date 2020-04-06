package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
private Toolbar toolbar;
private TextInputEditText status_input;
private Button status_button;
private DatabaseReference databaseReference;
    private FirebaseUser current_user;
    private ProgressDialog RegProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        current_user= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=current_user.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        RegProgress =new ProgressDialog(this);
        toolbar=findViewById(R.id.status_toolbar);


        status_button=findViewById(R.id.status_button);
        status_input=findViewById(R.id.status_input);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String current_status=getIntent().getStringExtra("Status");
        status_input.setText(current_status);

        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegProgress.setTitle("Saving Changes");
                RegProgress.setMessage("Please Wait");
                RegProgress.setCanceledOnTouchOutside(false);
                RegProgress.show();

                String status=status_input.getEditableText().toString();
                databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            RegProgress.dismiss();
                        }
                        else { RegProgress.hide();
                        Toast.makeText(StatusActivity.this,"ERROR Ha ha XD",Toast.LENGTH_LONG).show();}

                    }
                });
            }
        });

    }
}
