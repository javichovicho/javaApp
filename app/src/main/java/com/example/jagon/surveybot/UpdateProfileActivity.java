package com.example.jagon.surveybot;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText userName, userSurname;
    private Button finishUserDetailsEdit;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String userId, name, surname, email;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        userName = (EditText)findViewById(R.id.editTextUserName);
        userSurname = (EditText)findViewById(R.id.editTextUserSurname);
        finishUserDetailsEdit = (Button)findViewById(R.id.buttonEditDetails);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        try {
            userId = firebaseAuth.getUid();
            final DatabaseReference databaseReference = firebaseDatabase.getReference("User data/users/" + userId);
        }catch(NullPointerException e){
            Log.i("Exception error", e.toString());
        }
        databaseReference = firebaseDatabase.getReference("User data/users/" + userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                try {
                    name = user.getName();
                    surname = user.getSurname();
                    email = user.getEmail();
                }catch(NullPointerException e){
                    Log.i("Exception error", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateProfileActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });

        finishUserDetailsEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(name.equals("") && surname.equals("") && email.equals(""))){
                    name = userName.getText().toString();
                    surname = userSurname.getText().toString();
                    user = new User(name, surname, email, userId);
                    databaseReference.setValue(user);
                    finish();
                }else{
                    Log.i("Data Error", "No user data retrieved");
                }
            }
        });
    }
}
