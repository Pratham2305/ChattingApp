package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView usersList;
    private DatabaseReference databaseReference;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mAuth= FirebaseAuth.getInstance();

        toolbar=findViewById(R.id.main_all_users);


            setSupportActionBar(toolbar);
           getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("All Users");

        databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");
        mUserRef=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        usersList=findViewById(R.id.users_list);

        usersList.setLayoutManager(new LinearLayoutManager(this));


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(new FirebaseRecyclerOptions.Builder<Users>().setQuery(databaseReference,Users.class).build()) {
            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull Users model) {

                Picasso.with(AllUsersActivity.this).load(model.getThumb_image()).placeholder(R.drawable.default_avatar).into(holder.user_profile);
                holder.user_name.setText(model.getName());
                holder.user_status.setText(model.getStatus());

                final String user_id=getRef(position).getKey();
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(AllUsersActivity.this,ProfileActivity.class);
                        intent.putExtra("user_id",user_id);

                        Pair[] pairs= new Pair[3];
                        pairs[0]=new Pair<View,String>(holder.user_name,"nameTransition");
                        pairs[1]=new Pair<View,String>(holder.user_status,"statusTransition");
                        pairs[2]=new Pair<View,String>(holder.iv,"imageTransition");

                        ActivityOptions options= ActivityOptions.makeSceneTransitionAnimation(AllUsersActivity.this,pairs);


                        startActivity(intent);
                    }
                });

            }

            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                UserViewHolder userViewHolder = new UserViewHolder(view);
                return userViewHolder;
            }
        };

        usersList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }
    public static class UserViewHolder extends RecyclerView.ViewHolder{
        public  TextView user_name,user_status;
        public ImageView iv;
        public  CircleImageView user_profile;

        public UserViewHolder(View itemView) {
            super(itemView);

            user_name= itemView.findViewById(R.id.user_name);
            user_status=itemView.findViewById(R.id.user_status);
            user_profile=itemView.findViewById(R.id.user_profile_img);
            iv=itemView.findViewById(R.id.imageView6);


        }
      }


    @SuppressLint("RestrictedApi")
    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.user_menu,menu);

        MenuItem searchViewItem
                = menu.findItem(R.id.app_bar_search);

        SearchView searchView
                = (SearchView) MenuItemCompat
                .getActionView(searchViewItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                char[] chars = query.toLowerCase().toCharArray();
                boolean found = false;
                for (int i = 0; i < chars.length; i++) {
                    if (!found && Character.isLetter(chars[i])) {
                        chars[i] = Character.toUpperCase(chars[i]);
                        found = true;
                    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                        found = false;
                    }
                }

                String output = String.valueOf(chars);

                Query firebaseSearchQuery = databaseReference.orderByChild("name").startAt(output).endAt(output + "\uf8ff");

                FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(new FirebaseRecyclerOptions.Builder<Users>().setQuery(firebaseSearchQuery,Users.class).build()) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull Users model) {

                        holder.user_name.setText(model.getName());
                        holder.user_status.setText(model.getStatus());
                        Picasso.with(AllUsersActivity.this).load(model.getThumb_image()).placeholder(R.drawable.default_avatar).into(holder.user_profile);

                        final String user_id=getRef(position).getKey();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(AllUsersActivity.this,ProfileActivity.class);
                                intent.putExtra("user_id",user_id);

                                Pair[] pairs= new Pair[3];
                                pairs[0]=new Pair<View,String>(holder.user_name,"nameTransition");
                                pairs[1]=new Pair<View,String>(holder.user_status,"statusTransition");
                                pairs[2]=new Pair<View,String>(holder.iv,"imageTransition");

                                ActivityOptions options= ActivityOptions.makeSceneTransitionAnimation(AllUsersActivity.this,pairs);


                                startActivity(intent);
                            }
                        });

                    }

                    @Override
                    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                        UserViewHolder userViewHolder = new UserViewHolder(view);
                        return userViewHolder;
                    }
                };

                usersList.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                char[] chars =newText.toLowerCase().toCharArray();
                boolean found = false;
                for (int i = 0; i < chars.length; i++) {
                    if (!found && Character.isLetter(chars[i])) {
                        chars[i] = Character.toUpperCase(chars[i]);
                        found = true;
                    } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                        found = false;
                    }
                }

               String output= String.valueOf(chars);

                Query firebaseSearchQuery = databaseReference.orderByChild("name").startAt(output).endAt(output+ "\uf8ff");

                FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(new FirebaseRecyclerOptions.Builder<Users>().setQuery(firebaseSearchQuery,Users.class).build()) {
                    @Override
                    protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull Users model) {

                        holder.user_name.setText(model.getName());
                        holder.user_status.setText(model.getStatus());
                        Picasso.with(AllUsersActivity.this).load(model.getThumb_image()).placeholder(R.drawable.default_avatar).into(holder.user_profile);

                        final String user_id=getRef(position).getKey();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(AllUsersActivity.this,ProfileActivity.class);
                                intent.putExtra("user_id",user_id);

                                Pair[] pairs= new Pair[3];
                                pairs[0]=new Pair<View,String>(holder.user_name,"nameTransition");
                                pairs[1]=new Pair<View,String>(holder.user_status,"statusTransition");
                                pairs[2]=new Pair<View,String>(holder.iv,"imageTransition");

                                ActivityOptions options= ActivityOptions.makeSceneTransitionAnimation(AllUsersActivity.this,pairs);


                                startActivity(intent);
                            }
                        });

                    }

                    @Override
                    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout,parent,false);
                        UserViewHolder userViewHolder = new UserViewHolder(view);
                        return userViewHolder;
                    }
                };

                usersList.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();


                return false;

            }

        });



        if(menu instanceof MenuBuilder) {  //To display icon on overflow menu

            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuitem){
        super.onOptionsItemSelected(menuitem);


        switch (menuitem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(menuitem);


    }

}
