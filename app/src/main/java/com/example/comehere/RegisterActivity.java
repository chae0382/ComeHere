package com.example.comehere;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;

import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private ArrayAdapter adapter;
    private Spinner spinner;

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageView;

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;

    private String dburl = "https://comehere-cd02d-default-rtdb.firebaseio.com/";
    private UserData inputUser;

    private Dialog dialog;
    private Button ok_btn;
    private TextView messageTv;

    private Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Student card Image code
        imageView = (ImageView)findViewById(R.id.studentCardImage);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

        // Underline in login TextView
        TextView textView = (TextView)findViewById(R.id.loginButton);
        SpannableString content = new SpannableString("???????????????");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0); textView.setText(content);

        // login button click listener
        TextView loginButton = (TextView)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                RegisterActivity.this.startActivity(loginIntent);
            }
        });

        // school spinner
        spinner = (Spinner)findViewById(R.id.schoolSpinner);
        adapter = new ArrayAdapter<String>(this, R.layout.select_spinner, getResources().getStringArray(R.array.school));
        adapter.setDropDownViewResource(R.layout.custom_spinner);
        spinner.setAdapter(adapter);

        // password check alarm
        final EditText pwcheck = (EditText)findViewById(R.id.passwordCheckText);
        final EditText pw = (EditText)findViewById(R.id.passwordText);
        final ImageView setImage = (ImageView)findViewById(R.id.pwImage);
        setImage.setImageResource(R.drawable.pwno);
        pwcheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (pw.getText().toString().equals(pwcheck.getText().toString())) {
                    // password and password check is same
                    setImage.setImageResource(R.drawable.pwok);
                }else {
                    // password and password check is not same
                    setImage.setImageResource(R.drawable.pwno);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (pw.getText().toString().equals(pwcheck.getText().toString())) {
                    // password and password check is same
                    setImage.setImageResource(R.drawable.pwok);
                }else {
                    // password and password check is not same
                    setImage.setImageResource(R.drawable.pwno);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // register button click listener
        Button register_btn = (Button)findViewById(R.id.registerButton);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        dialog = new Dialog(this);
    }

    public void ShowOkPopup() {
        dialog.setContentView(R.layout.epic_popup);
        messageTv = (TextView)dialog.findViewById(R.id.dialogMessageText);
        ok_btn = (Button)dialog.findViewById(R.id.okbtn);
        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                RegisterActivity.this.startActivity(intent);
                finish();
                startToast(inputUser.getNickname() + "?????? ????????? ???????????????!.");
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void Dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setMessage("???????????? ??????!\n?????? ?????? ?????? ????????????\n??????????????? ??????????????????");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                startToast(inputUser.getNickname() + "?????? ????????? ???????????????!");
            }
        });

        builder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imgUri = data.getData();
            imageView.setImageURI(imgUri);
        }

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {
                    // select image dir get
                    imgUri = data.getData();
                    imageView.setImageURI(imgUri);
                    Glide.with(this).load(imgUri).into(imageView);
                }
                break;
        }
    }

    // sign up function
    private void signUp() {
        String email = ((EditText)findViewById(R.id.emailText)).getText().toString();
        String password = ((EditText)findViewById(R.id.passwordText)).getText().toString();
        String passwordCheck = ((EditText)findViewById(R.id.passwordCheckText)).getText().toString();

        if (email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
            if (password.equals(passwordCheck) && imgUri != null) {
                // password same password check
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    // User data
                                    String uid = user.getUid();
                                    String nickname = ((EditText)findViewById(R.id.nicknameText)).getText().toString().trim();
                                    String school = ((Spinner)findViewById(R.id.schoolSpinner)).getSelectedItem().toString();
                                    Integer sId = Integer.parseInt(((EditText)findViewById(R.id.studentIdText)).getText().toString().trim());

                                    // Firebase Storage object
                                    FirebaseStorage storage = FirebaseStorage.getInstance();

                                    StorageReference imgRef = null;

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
                                    String filename = nickname+sdf.format(new Date())+".png";
                                    String studentIdCard = "studentCard/"+filename;

                                    StorageReference sRef = storage.getReference("studentCard/"+filename);
                                    sRef.putFile(imgUri);

                                    // User object save in firebase
                                    inputUser = new UserData(uid, nickname, school, sId, studentIdCard);

                                    FirebaseDatabase database= FirebaseDatabase.getInstance(dburl);
                                    DatabaseReference reference = database.getReference("User");
                                    reference.child(uid).setValue(inputUser);

                                    // set current user profile
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(nickname) // if you want add the profile photo
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User profile updated");
                                                    }
                                                }
                                            });

                                    // UI Logic when success
                                    ShowOkPopup();
//                                    Dialog();
                                } else {
                                    if (task.getException() != null) {
                                        // If sign in fails, display a message to the user.
                                        try {
                                            throw task.getException();
                                        }catch (FirebaseAuthUserCollisionException e) {
                                            startToast("?????? ???????????? ??????????????????.");
                                            findViewById(R.id.emailText).requestFocus();
                                        } catch (FirebaseAuthWeakPasswordException e) {
                                            startToast("??????????????? 6?????? ?????? ??????????????????.");
                                            findViewById(R.id.passwordText).requestFocus();
                                        } catch (Exception e) {
                                            startToast(e.toString());
                                        }

//                                        startToast(task.getException().toString());
                                        // UI Logic when failed
                                    }
                                }
                            }
                        });

            }else if(!password.equals(passwordCheck)) {
                // password not same passwordcheck
                startToast("??????????????? ???????????? ????????????.");
            }else if (imgUri == null) {
                startToast("???????????? ??????????????????.");
            }
        }else {
            startToast("????????? ?????? ??????????????? ??????????????????.");
        }

    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
