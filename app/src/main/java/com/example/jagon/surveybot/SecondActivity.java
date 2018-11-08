package com.example.jagon.surveybot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    ArrayList<String> modules = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    private TextView greetingTextView;
    private String userName;
    private String userId;
    private String greeting = "Hi!";

    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceChild;

    private ArrayList<User> users;
    private ArrayAdapter<User> adapter;
    private User usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle("Modules list");

        firebaseAuth = FirebaseAuth.getInstance();

        ListView modulesListView = (ListView)findViewById(R.id.modulesListView);
        greetingTextView = (TextView)findViewById(R.id.textViewGreeting);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceChild = databaseReference.child("User data").child("users").child(userId);

        usuario = new User();

        databaseReferenceChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue(String.class);
                Log.i("info", userName + " - user name retrieved from database");
                Toast.makeText(SecondActivity.this, "Hi " + userName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("info", databaseError.getMessage());
            }
        });

        //greeting += " " + userName + "!";
        greetingTextView.setText(greeting);

        modulesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("moduleName", modules.get(position));
                startActivity(intent);
            }
        });

        modules.add("Introduction to Databases");
        modules.add("Software Development");

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modules);
        modulesListView.setAdapter(arrayAdapter);

    }

    private void logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(SecondActivity.this, MainActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    // to handle the onclick methods of items in menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logoutButton:{
                logout();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
