package com.example.jagon.surveybot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
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
    private DatabaseReference databaseReferenceModules;
    private FirebaseDatabase database;

    private ArrayList<User> users;
    private ArrayAdapter<User> adapter;
    private User usuario;
    private Module module1;
    private ListView modulesListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle("Modules list");

        firebaseAuth = FirebaseAuth.getInstance();

        modulesListView = (ListView)findViewById(R.id.modulesListView);
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

        // At the moment greeting is just Hi
        greetingTextView.setText(greeting);

        modulesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("moduleName", modules.get(position));
                startActivity(intent);
            }
        });

        // Uploading a model to the database
        /*
        Module module1 = new Module("Introduction to Databases");
        database = FirebaseDatabase.getInstance();
        databaseReferenceModules = database.getReference("Module data");
        databaseReferenceModules.child("modules").child(module1.getId()).setValue(module1);
        */
        // Retrieving a list of all models in database
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modules);
        module1 = new Module();
        database = FirebaseDatabase.getInstance();
        databaseReferenceModules = database.getReference("Module data/modules");
        databaseReferenceModules.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    module1 = snapshot.getValue(Module.class);
                    modules.add(module1.getTitle());
                }
                modulesListView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
