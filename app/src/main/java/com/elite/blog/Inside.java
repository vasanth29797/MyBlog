package com.elite.blog;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Inside extends AppCompatActivity {

    private ImageView iv_inside;
    private TextView tv_inside_title, tv_inside_desc;
    private Button btn_inside;
    private FirebaseAuth auth;
    private String uid=null;
    private DatabaseReference posts_db_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside);

        auth=FirebaseAuth.getInstance();

        iv_inside=findViewById(R.id.iv_inside);
        tv_inside_title=findViewById(R.id.tv_inside_title);
        tv_inside_desc=findViewById(R.id.tv_inside_desc);
        btn_inside=findViewById(R.id.btn_inside);

        Intent outside=getIntent();
        String post=outside.getStringExtra("post");

        posts_db_ref= FirebaseDatabase.getInstance().getReference().child("Posts");
        posts_db_ref=posts_db_ref.child(post);
        posts_db_ref.keepSynced(true);

        posts_db_ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String link=dataSnapshot.child("image").getValue(String.class);
                        String title=dataSnapshot.child("title").getValue(String.class);
                        String desc=dataSnapshot.child("desc").getValue(String.class);
                        uid=dataSnapshot.child("uid").getValue(String.class);

                        Glide.with(getApplicationContext()).load(link).thumbnail(0.5f).crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL).into(iv_inside);

                        tv_inside_title.setText(title);
                        tv_inside_desc.setText(desc);

                        if(auth.getCurrentUser().getUid().equals(uid)) {
                            btn_inside.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        btn_inside.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(auth.getCurrentUser().getUid().equals(uid)) {
                            posts_db_ref.removeValue();
                            Toast.makeText(Inside.this, "Post deleted!", Toast.LENGTH_SHORT).show();
                            Intent outside=new Intent(Inside.this, MainActivity.class);
                            outside.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(outside);
                        }
                    }
                }
        );

    }
}
