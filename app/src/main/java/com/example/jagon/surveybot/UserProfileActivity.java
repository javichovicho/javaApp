package com.example.jagon.surveybot;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userName, userSurname, userEmail;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String userId;
    private Button updateDetailsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userName = (TextView)findViewById(R.id.textViewUserName);
        userSurname = (TextView)findViewById(R.id.textViewUserSurname);
        userEmail = (TextView)findViewById(R.id.textViewUserEmail);
        updateDetailsButton = (Button)findViewById(R.id.buttonUserDetailsUpdate);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        try{
            userId = firebaseAuth.getUid();
            databaseReference = firebaseDatabase.getReference("User data/users/" + userId);
        }catch(NullPointerException e){
            Log.i("Exception error", e.toString());
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                try {
                    userName.setText("Name: " + user.getName());
                    userSurname.setText("Surname: " + user.getSurname());
                    userEmail.setText("Email: " + user.getEmail());
                }catch(NullPointerException e){
                    Log.i("Exception error", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });

        updateDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserProfileActivity.this, UpdateProfileActivity.class));
            }
        });
    }
}
