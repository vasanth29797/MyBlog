package com.elite.blog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private RecyclerView rv;
    private DatabaseReference posts_db_ref, users_db_ref, likes_db_ref;
    private Query query;//
    private static final String TAG = "check";
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener asl;
    private Boolean like = false;
    private String uid=null;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        posts_db_ref = FirebaseDatabase.getInstance().getReference().child("Posts");
        posts_db_ref.keepSynced(true);

        users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        users_db_ref.keepSynced(true);

        likes_db_ref = FirebaseDatabase.getInstance().getReference().child("Likes");
        likes_db_ref.keepSynced(true);

        auth=FirebaseAuth.getInstance();
        uid=auth.getCurrentUser().getUid();//

        query=posts_db_ref.orderByChild("uid").equalTo(uid);//

        rv = (RecyclerView) findViewById(R.id.rv);

        LinearLayoutManager ll_manager = new LinearLayoutManager(this);
        ll_manager.setReverseLayout(true);
        ll_manager.setStackFromEnd(true);

        rv.setHasFixedSize(true);
        rv.setLayoutManager(ll_manager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> adapter
                = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.single_post,
                BlogViewHolder.class,
                query//posts_db_ref
        ) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {

                final String post_id = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setPp(getApplicationContext(), model.getPp());
                viewHolder.setUname(model.getUname());
                viewHolder.setTime(model.getTime());

                viewHolder.setLikeBtn(post_id);

                viewHolder.view.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Toast.makeText(MainActivity.this, post_id, Toast.LENGTH_SHORT).show();
                                Intent inside = new Intent(Profile.this, Inside.class);
                                inside.putExtra("post", post_id);
                                startActivity(inside);
                            }
                        }
                );

                viewHolder.ib_post_like.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                like = true;

                                likes_db_ref.addValueEventListener(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (like) {
                                                    if (dataSnapshot.child(post_id).hasChild(auth.getCurrentUser().getUid())) {
                                                        likes_db_ref.child(post_id).child(auth.getCurrentUser().getUid()).removeValue();
                                                        like = false;
                                                    } else {
                                                        likes_db_ref.child(post_id).child(auth.getCurrentUser().getUid()).setValue("xxx");
                                                        like = false;
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );
                            }
                        }
                );

            }
        };

        rv.setAdapter(adapter);
    }


    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageButton ib_post_like;
        DatabaseReference mlike_db_ref;
        FirebaseAuth mAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            ib_post_like = view.findViewById(R.id.ib_post_like);
            mlike_db_ref=FirebaseDatabase.getInstance().getReference().child("Likes");
            mlike_db_ref.keepSynced(true);
            mAuth=FirebaseAuth.getInstance();
            /*tv_post_title=(TextView) view.findViewById(R.id.tv_post_title);
            tv_post_title.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i(TAG, "title");
                        }
                    }
            );*/
        }



        public void setTitle(String title) {
            TextView tv_post_title = (TextView) view.findViewById(R.id.tv_post_title);
            tv_post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView tv_post_desc = (TextView) view.findViewById(R.id.tv_post_desc);
            tv_post_desc.setText(desc);
        }

        public void setUname(String uname) {
            TextView tv_post_uname = view.findViewById(R.id.tv_post_uname);
            tv_post_uname.setText(uname);
        }

        public void setTime(String time) {
            TextView tv_post_time = view.findViewById(R.id.tv_post_time);
            tv_post_time.setText(time);
        }

        public void setImage(Context context, String image) {
            ImageView iv_post = (ImageView) view.findViewById(R.id.iv_post);
            Glide.with(context).load(image).thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv_post);
        }

        public void setPp(Context context, String pp) {
            ImageView iv_post_pp = view.findViewById(R.id.iv_post_pp);
            Glide.with(context).load(pp).thumbnail(0.5f)
                    .crossFade()
                    .bitmapTransform(new GlideCircleTransformation(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(iv_post_pp);
        }

        public void setLikeBtn(final String post_id) {
            mlike_db_ref.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(post_id).hasChild(mAuth.getCurrentUser().getUid())) {
                                ib_post_like.setImageResource(R.drawable.ic_thumb_up_color_accent_24dp);
                            }
                            else {
                                ib_post_like.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    }
            );
        }
    }
}
