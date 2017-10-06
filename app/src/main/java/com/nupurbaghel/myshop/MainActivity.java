package com.nupurbaghel.myshop;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText emailText;
    EditText passwordText;
    Button signup;
    Button register;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    String TAG = "Mymsg";
    private ProgressDialog progressDialog;


    @Override
    public void onBackPressed() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailText= (EditText)findViewById(R.id.emailText);
        passwordText=(EditText)findViewById(R.id.passwordText);
        signup=(Button)findViewById(R.id.signupbutton);
        register=(Button) findViewById(R.id.registerButton);
        progressDialog=new ProgressDialog(this);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"Register clicked");
                startRegister();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"SignUp clicked");
                startSignIn();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.i(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                } else {
                    // User is signed out
                    Log.i(TAG, "onAuthStateChanged:signed_out");
                }
             }
        };

        TextView forgetTextView= (TextView) findViewById(R.id.forgetPass);
        forgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString();
                if(email.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter email id", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.setMessage("Sending Mail...");
                    progressDialog.show();
                    resetMail(email);
                }
            }
        });
    }

    public void resetMail(String emailAddress){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.cancel();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(MainActivity.this, "Email Sent Successfully!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "There was a problem in sending email, try again later...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void startSignIn(){

        String email=emailText.getText().toString();
        String password=passwordText.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
                Toast.makeText(MainActivity.this, "Fields are empty", Toast.LENGTH_LONG).show();
            }
            else if(TextUtils.isEmpty(email)){
                Toast.makeText(MainActivity.this, "Email field is empty", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Password field is empty", Toast.LENGTH_LONG).show();
            }

        }
        else {
            progressDialog.setMessage("Signing in User");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        progressDialog.cancel();
                        Log.e(TAG, "Sign-in Failed: " + task.getException().getMessage());
                        String temp=task.getException().getMessage();

                        if(temp.indexOf("no user record corresponding to this identifier") != -1){
                            String display = "No such user found. Register first";
                            Toast.makeText(MainActivity.this, display, Toast.LENGTH_LONG).show();
                        }
                        else if(temp.indexOf("password is invalid") != -1){
                            String display = "Invalid password!";
                            Toast.makeText(MainActivity.this, display, Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }
            });
        }

    }

    private void startRegister(){

        String email=emailText.getText().toString();
        String password=passwordText.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
                Toast.makeText(MainActivity.this, "Fields are empty", Toast.LENGTH_LONG).show();
            }
            else if(TextUtils.isEmpty(email)){
                Toast.makeText(MainActivity.this, "Email field is empty", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Password field is empty", Toast.LENGTH_LONG).show();
            }

        }
        else {
            progressDialog.setMessage("Registering User");
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (!task.isSuccessful()) {
                        progressDialog.cancel();
                        Log.e(TAG, "Register Failed: " + task.getException().getMessage());
                        String temp=task.getException().getMessage();

                        if(temp.indexOf("WEAK_PASSWORD") != -1){
                            String display = "Password must consist of atleast 6 characters";
                            Toast.makeText(MainActivity.this, display, Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

    }
}
