package com.example.jagon.surveybot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordRecoveryActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private Button passwordResetButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recovery);

        passwordEditText = (EditText)findViewById(R.id.editTextEmail);
        passwordResetButton = (Button)findViewById(R.id.buttonPasswordReset);
        firebaseAuth = FirebaseAuth.getInstance();

        passwordResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = passwordEditText.getText().toString().trim();
                if(userEmail.equals("")){
                    Toast.makeText(PasswordRecoveryActivity.this, "Please provide a valid email", Toast.LENGTH_SHORT).show();
                }else{
                    firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(PasswordRecoveryActivity.this, "Recovery Email sent!", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(PasswordRecoveryActivity.this, MainActivity.class));
                            }else{
                                Toast.makeText(PasswordRecoveryActivity.this, "Email not found!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
