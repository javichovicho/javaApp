package com.example.jagon.surveybot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ModuleCreationActivity extends AppCompatActivity {

    private EditText editText;
    private Button button;
    private String moduleTitle;
    private Module module;

    private FirebaseDatabase database;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_creation);

        editText = (EditText)findViewById(R.id.moduleEditText);
        button = (Button)findViewById(R.id.createModuleButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moduleTitle = editText.getText().toString();
                if(moduleTitle.matches("")){
                    Toast.makeText(ModuleCreationActivity.this, "You did not enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    module = new Module(moduleTitle);

                    database = FirebaseDatabase.getInstance();
                    reference = database.getReference("Module data");
                    reference.child("modules").child(module.getId()).setValue(module);
                    goToPreviousActivity();
                }
            }
        });
    }

    private void goToPreviousActivity(){
        finish();
        startActivity(new Intent(ModuleCreationActivity.this, SecondActivity.class));
    }
}
