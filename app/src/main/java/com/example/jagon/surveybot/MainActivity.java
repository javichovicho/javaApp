package com.example.jagon.surveybot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    private EditText Email;
    private EditText Password;
    private TextView Info;
    private TextView userRegistration;
    private TextView passwordRecovery;
    private Button Login;
    private int counter = 5;
    private boolean emailVerified;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Email = (EditText)findViewById(R.id.email);
        Password = (EditText)findViewById(R.id.password);
        Info = (TextView)findViewById(R.id.info);
        passwordRecovery = (TextView)findViewById(R.id.textViewPasswordRecovery);
        userRegistration = (Button)findViewById(R.id.registration);
        Login = (Button)findViewById(R.id.loginButton);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        // The following checks for a signed in User
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            finish();
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateUserLoginDetails(Email.getText().toString(), Password.getText().toString());
            }
        });

        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        passwordRecovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PasswordRecoveryActivity.class));
            }
        });

    }
    private void validateUserLoginDetails(String userEmail, String userPassword){

        progressDialog.setMessage("Verifying account...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    emailVerificationCheck();
                }else{
                    Toast.makeText(MainActivity.this,  "Login Failed", Toast.LENGTH_SHORT).show();
                    counter--;
                    Info.setText("Number of attempts reamining: " + counter);
                    if(counter == 0){
                        Login.setEnabled(false);
                    }
                }
            }
        });
    }

    private void emailVerificationCheck(){
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();

        try {
            emailVerified = firebaseUser.isEmailVerified();
        }catch(NullPointerException e){
            Log.i("Exception", e.toString());
        }

        if(emailVerified){
            // Toast.makeText(MainActivity.this,  "Login Successful", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(MainActivity.this, SecondActivity.class));
        }else{
            Toast.makeText(this, "Please make sure your email is verified", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }
}

