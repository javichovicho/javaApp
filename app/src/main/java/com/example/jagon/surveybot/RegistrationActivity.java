package com.example.jagon.surveybot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private EditText firstName, lastName, userPassword, userEmail;
    private Button registerButton;
    private Button Login;
    private FirebaseAuth firebaseAuth;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setUIViews();

        firebaseAuth = FirebaseAuth.getInstance();


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    // Once the registration details have been validated store data in database
                    String user_email = userEmail.getText().toString().trim();
                    String user_password = userPassword.getText().toString().trim();

                    // Add on complete allows for a confirmation message if successful
                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                /*
                                Toast.makeText(RegistrationActivity.this,
                                        "Registration successful", Toast.LENGTH_SHORT).show();
                                storeUserInfo(firstName.getText().toString(), lastName.getText().toString(),
                                        userPassword.getText().toString(), userEmail.getText().toString());
                                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                                */
                                sendVerificationEmail();
                            }else{
                                Toast.makeText(RegistrationActivity.this,
                                        "Registration unsuccessful", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        Login = (Button)findViewById(R.id.login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
            }
        });
    }

    private void setUIViews(){
        firstName = (EditText)findViewById(R.id.firstName);
        lastName = (EditText)findViewById(R.id.lastName);
        userEmail = (EditText)findViewById(R.id.email);
        userPassword = (EditText)findViewById(R.id.password);
        registerButton = (Button)findViewById(R.id.button);
    }

    private Boolean validate() {
        Boolean result = false;
        String name = firstName.getText().toString();
        String surname = lastName.getText().toString();
        String password = userPassword.getText().toString();
        String email = userEmail.getText().toString();

        if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT);
        } else {
            result = true;
        }
        return result;
    }

    private void storeUserInfo(String name, String surname, String password, String email){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        User newUser = new User(name, surname, email, password, uid);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("User data");
        myRef.child("users").child(uid).setValue(newUser);

    }

    private void sendVerificationEmail(){
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        storeUserInfo(firstName.getText().toString(), lastName.getText().toString(),
                                userPassword.getText().toString(), userEmail.getText().toString());
                        Toast.makeText(RegistrationActivity.this, "You are now registered, verification mail sent",
                                Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(RegistrationActivity.this, MainActivity.class));
                    }else{
                        Toast.makeText(RegistrationActivity.this, "Verification mail could not be sent",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
