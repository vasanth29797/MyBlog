package com.elite.blog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SigninActivity extends AppCompatActivity {

    private EditText et_signin_email, et_signin_pwd;
    private FirebaseAuth auth;
    private DatabaseReference users_db_ref;
    private ProgressBar pb_signin;

    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN=1;

    private static final String TAG="check";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        auth=FirebaseAuth.getInstance();
        users_db_ref= FirebaseDatabase.getInstance().getReference().child("Users");
        users_db_ref.keepSynced(true);

        et_signin_email=(EditText) findViewById(R.id.et_signin_email);
        et_signin_pwd=(EditText) findViewById(R.id.et_signin_pwd);

        Button btn_signin=(Button) findViewById(R.id.btn_signin);
        Button btn_signin_new=(Button) findViewById(R.id.btn_signin_new);
        
        pb_signin=(ProgressBar) findViewById(R.id.pb_signin);

        SignInButton btn_signin_google=(SignInButton) findViewById(R.id.btn_signin_google);

        btn_signin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkSignin();
                    }
                }
        );


        btn_signin_new.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent signup=new Intent(SigninActivity.this, SignupActivity.class);
                        startActivity(signup);

                    }
                }
        );

        //configure google sign-in
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        btn_signin_google.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signIn();
                    }
                }
        );

    }

    private void signIn() {

        if (googleApiClient.isConnected())
            googleApiClient.clearDefaultAccountAndReconnect();

        Intent signInIntent= Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN) {
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            pb_signin.setVisibility(View.VISIBLE);

            if(result.isSuccess()) {
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else {
                pb_signin.setVisibility(View.GONE);
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            pb_signin.setVisibility(View.GONE);
                            checkUserExists();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            pb_signin.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void checkSignin() {

        String email=et_signin_email.getText().toString().trim();
        String pwd=et_signin_pwd.getText().toString().trim();

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
            
            pb_signin.setVisibility(View.VISIBLE);
            
            auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pb_signin.setVisibility(View.GONE);
                            checkUserExists();
                        }
                    }
            ).addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pb_signin.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Err, Sign in unsuccessful!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }

    }

    private void checkUserExists() {//exists in database?

        if(auth.getCurrentUser()!=null) {

            final String uid = auth.getCurrentUser().getUid();

            users_db_ref.addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(uid)) {
                                Intent home = new Intent(SigninActivity.this, MainActivity.class);
                                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(home);
                            } else {
                                Intent setup_account = new Intent(SigninActivity.this, AccountSetupActivity.class);
                                setup_account.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(setup_account);
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
