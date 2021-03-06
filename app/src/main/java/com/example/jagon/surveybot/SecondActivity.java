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

    private final String TITLE = "Modules list";
    private ListView modulesListView;
    private TextView greetingTextView;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String userId;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceChild;
    private String userName;

    ArrayList<String> modules = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    private String greeting = "Hi!";

    private DatabaseReference databaseReferenceModules;
    private FirebaseDatabase database;

    private Module module1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTitle(TITLE);

        modulesListView = (ListView)findViewById(R.id.modulesListView);
        greetingTextView = (TextView)findViewById(R.id.textViewGreeting);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceChild = databaseReference.child("User data").child("users").child(userId);

        databaseReferenceChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("name").getValue(String.class);
                Log.i("info", userName + " - user name retrieved from database");
                // This toast is overlapping login successful which is now disabled
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
                //Intent intent = new Intent(getApplicationContext(), ChatActivity.class);

                // link to SurveyActivity
                Intent intent = new Intent(getApplicationContext(), SurveyActivity.class);

                intent.putExtra("moduleName", modules.get(position));
                startActivity(intent);
            }
        });

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
    private void openModuleCreationActivity(){
        startActivity(new Intent(SecondActivity.this, ModuleCreationActivity.class));
    }
    private void openUserProfileActivity(){
        startActivity(new Intent(SecondActivity.this, UserProfileActivity.class));
    }
    /*private void openDialogActivity(){
        startActivity(new Intent(SecondActivity.this, DialogActivity.class));
    }
    private void openSurveyActivity(){
        startActivity(new Intent(SecondActivity.this, SurveyActivity.class));
    }*/

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
                return true;
            }
            case R.id.moduleCreation:{
                openModuleCreationActivity();
                return true;
            }
            case R.id.editProfile:{
                openUserProfileActivity();
                return true;
            }
            /*case R.id.dialog:{
                openDialogActivity();
                return true;
            }
            case R.id.surveyTemp:{
                openSurveyActivity();
                return true;
            }*/
        }
        return super.onOptionsItemSelected(item);
    }
}
