package com.elite.blog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText et_signup_name, et_signup_email, et_signup_pwd;
    private Button bt_signup;
    private FirebaseAuth auth;
    private DatabaseReference users_db_ref;
    private ProgressBar pb;
    private String uid=null;
    private static final String TAG="check";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth=FirebaseAuth.getInstance();
        users_db_ref=FirebaseDatabase.getInstance().getReference().child("Users");

        et_signup_name=(EditText) findViewById(R.id.et_signup_name);
        et_signup_email=(EditText) findViewById(R.id.et_signup_email);
        et_signup_pwd=(EditText) findViewById(R.id.et_signup_pwd);

        bt_signup=(Button) findViewById(R.id.btn_signup);

        pb=(ProgressBar) findViewById(R.id.pb_signup);

        bt_signup.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signup();
                    }
                }
        );

    }

    private void signup() {

        final String name=et_signup_name.getText().toString().trim();
        String email=et_signup_email.getText().toString().trim();
        final String pwd=et_signup_pwd.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {

            pb.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()) {

                                uid=auth.getCurrentUser().getUid();

                                DatabaseReference child_db_ref=users_db_ref.child(uid);
                                child_db_ref.child("name").setValue(name);
                                child_db_ref.child("image").setValue("default");

                                pb.setVisibility(View.GONE);

                                Intent home=new Intent(SignupActivity.this, MainActivity.class);
                                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(home);
                            }
                        }
                    }
            ).addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pb.setVisibility(View.GONE);
                            Log.i(TAG, "Error: "+e.toString());
                            Toast.makeText(getApplicationContext(), "Err, Cannot sign up!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

    }
}

