package com.elite.blog;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class AccountSetupActivity extends AppCompatActivity {

    private static final String TAG="check";
    private ImageButton ib_account;
    private EditText et_account_name;

    private static final int GALLERY_REQUEST=1;

    private Uri final_image_uri=null;

    private FirebaseAuth auth;
    private DatabaseReference users_db_ref;
    private StorageReference root_sto_ref;

    private ProgressBar pb_account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);

        auth=FirebaseAuth.getInstance();
        users_db_ref= FirebaseDatabase.getInstance().getReference().child("Users");
        root_sto_ref= FirebaseStorage.getInstance().getReference().child("Users");

        ib_account=(ImageButton) findViewById(R.id.ib_account);
        et_account_name=(EditText) findViewById(R.id.et_account_name);
        Button btn_account=(Button) findViewById(R.id.btn_account);

        pb_account=(ProgressBar) findViewById(R.id.pb_account);

        ib_account.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(Intent.ACTION_PICK);//if ACTION_GET_CONTENT is used, all the documents too will be
                        //dispalyed
                        intent.setType("image/*");//makes the documents to blur in case of ACTION_GET_CONTENT
                        //if it is not used in case of ACTION_PICK, no images will be displayed but will open a bottom sheet
                        //with unnecessary contents
                        startActivityForResult(intent, GALLERY_REQUEST);
                    }
                }
        );

        btn_account.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setupAccount();
                    }
                }
        );

    }

    private void setupAccount() {
        final String name=et_account_name.getText().toString().trim();
        final String uid=auth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name) && uid!=null) {

            pb_account.setVisibility(View.VISIBLE);

            StorageReference user_sto_ref=root_sto_ref.child(final_image_uri.getLastPathSegment());
            user_sto_ref.putFile(final_image_uri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            String dl_url=taskSnapshot.getDownloadUrl().toString();

                            users_db_ref.child(uid).child("name").setValue(name);
                            users_db_ref.child(uid).child("image").setValue(dl_url);

                            pb_account.setVisibility(View.GONE);

                            Toast.makeText(getApplicationContext(), "Account setup successful!", Toast.LENGTH_SHORT).show();

                            Intent home=new Intent(AccountSetupActivity.this, MainActivity.class);
                            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(home);

                        }
                    }
            );

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK) {

            Uri image_uri=data.getData();
            CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK) {
                final_image_uri=result.getUri();
                ib_account.setImageURI(final_image_uri);
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error=result.getError();
            }
        }

    }
}
