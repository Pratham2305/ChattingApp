package com.example.chattingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int MSG_TYPE_RIGHT = 0;
    private static final int MSG_TYPE_LEFT = 1;
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList=mMessageList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profileImage;
        public ConstraintLayout constraintLayout;
        public ImageView message_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.text_message_incoming);
            profileImage=itemView.findViewById(R.id.message_profile_layout);
            constraintLayout=itemView.findViewById(R.id.constraintLayout);
            message_image=itemView.findViewById(R.id.message_image_layout);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_LEFT) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_left, parent, false);
            return new MessageViewHolder(v);
        }
        else {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_right, parent, false);
            return new MessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Messages c = mMessageList.get(position);
        final String from_user = c.getFrom();

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                String current_user_id= mAuth.getCurrentUser().getUid();

                if (!current_user_id.equals(from_user)){
                    Picasso.with(holder.message_image.getContext()).load(image).placeholder(R.drawable.default_avatar).into(holder.profileImage);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        if (c.getType().equals("text")){

        String current_user_id= mAuth.getCurrentUser().getUid();

        if (from_user.equals(current_user_id)){



        }else {
        }

        holder.messageText.setText(c.getMessage());
        holder.message_image.setVisibility(View.INVISIBLE);


    }

if (c.getType().equals("image")){

    holder.messageText.setVisibility(View.INVISIBLE);

    holder.message_image.setVisibility(View.VISIBLE);

    Picasso.with(holder.message_image.getContext()).load(c.getMessage()).placeholder(R.drawable.default_avatar).into(holder.message_image);

}
    }


    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (mMessageList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid())) {
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }
}
