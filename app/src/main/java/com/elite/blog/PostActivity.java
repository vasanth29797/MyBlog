package com.elite.blog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostActivity extends AppCompatActivity {

    private ImageButton ib_post;
    private static final int GALLERY_REQUEST=1;
    private EditText et_post_title, et_post_desc;
    private Button btn_post;
    private Uri uri=null;
    private StorageReference root_sto_ref, post_image_sto_ref;
    private ProgressBar pb;
    private DatabaseReference root_db_ref, posts_db_ref, users_db_ref;
    private static final String TAG="check";
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();

        root_sto_ref= FirebaseStorage.getInstance().getReference();
        root_db_ref= FirebaseDatabase.getInstance().getReference();

        users_db_ref=root_db_ref.child("Users").child(user.getUid());

        ib_post=(ImageButton) findViewById(R.id.ib_post);
        et_post_title=(EditText) findViewById(R.id.et_post_title);
        et_post_desc=(EditText) findViewById(R.id.et_post_desc);
        btn_post=(Button) findViewById(R.id.btn_post);
        pb=(ProgressBar) findViewById(R.id.pb_post);

        ib_post.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore
                                .Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, GALLERY_REQUEST);
                    }
                }
        );

        btn_post.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startPosting();
                    }
                }
        );

    }

    private void startPosting() {

        final String post_title=et_post_title.getText().toString().trim();
        final String post_desc=et_post_desc.getText().toString().trim();

        if(!TextUtils.isEmpty(post_title) && !TextUtils.isEmpty(post_desc) && uri!=null) {
            
            pb.setVisibility(View.VISIBLE);
            
            post_image_sto_ref=root_sto_ref.child("Post_Images");

            post_image_sto_ref.child(uri.getLastPathSegment()).putFile(uri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {//pushing into db only after pushing image of the post
                            //into storage

                            final Uri dl_uri=taskSnapshot.getDownloadUrl();

                            users_db_ref.addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            posts_db_ref=root_db_ref.child("Posts").push();//push() generates a random unique id
                                            posts_db_ref.child("title").setValue(post_title);
                                            posts_db_ref.child("desc").setValue(post_desc);

                                            if(dl_uri!=null)
                                                posts_db_ref.child("image").setValue(dl_uri.toString());

                                            posts_db_ref.child("uid").setValue(user.getUid());//useful in profile page display

                                            SimpleDateFormat dateFormat=new SimpleDateFormat("d/M/yy hh:mm");
                                            Date date=new Date();
                                            String time=dateFormat.format(date);
                                            posts_db_ref.child("time").setValue(time);

                                            posts_db_ref.child("pp").setValue(dataSnapshot.child("image").getValue());

                                            posts_db_ref.child("uname").setValue(dataSnapshot.child("name").getValue())
                                                .addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                pb.setVisibility(View.GONE);
                                                                Toast.makeText(getApplicationContext(), "Successfully posted!", Toast.LENGTH_SHORT)
                                                                        .show();

                                                                startActivity(new Intent(PostActivity.this, MainActivity.class));
                                                            }
                                                        }
                                                );
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    }
                            );
                        }
                    }
            ).addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Err, Cannot be posted", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
            );
        }
        else {
            Toast.makeText(getApplicationContext(), "Err, Field(s) are empty!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK) {
            try {
                uri=data.getData();
                InputStream inputStream=getContentResolver().openInputStream(uri);
                Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                ib_post.setImageBitmap(bitmap);
            }
            catch (FileNotFoundException ex) {

            }
        }

    }
}
