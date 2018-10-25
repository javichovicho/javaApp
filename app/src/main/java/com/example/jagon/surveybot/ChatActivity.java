package com.example.jagon.surveybot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private String activeModule = "";
    //private Button sendChatButton;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private Message message;
    private Message botMessage;
    private EditText chatEditText;

    private ArrayList<String> messages;
    private ArrayAdapter<String> adapter;

    private int counter = 0;
    private String response = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        activeModule = intent.getStringExtra("moduleName");

        setTitle(activeModule);

        Log.i("Info", activeModule);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("data");

        //sendChatButton = (Button)findViewById(R.id.sendChatButton);

        ListView chatListView = (ListView)findViewById(R.id.chatListView);

        messages = new ArrayList<>();
        messages.add("Hello, welcome to the " + activeModule + " module survey, how are you today?");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(adapter);
    }

    public void sendChat(View view){
        chatEditText = (EditText)findViewById(R.id.chatEditText);

        message = new Message(activeModule, chatEditText.getText().toString());
        messages.add("> " + message.getMessage());

        adapter.notifyDataSetChanged();

        String id = message.getId();
        //DatabaseReference messagesRef = myRef.child("messages").child(id).setValue(message);
        myRef.child("messages").child(id).setValue(message);
        //messagesRef.setValue(message);

        Log.i("Message saved", message.toString());
        Toast.makeText(ChatActivity.this,  "Message sent", Toast.LENGTH_SHORT).show();
        chatEditText.setText("");

        // So that the keyboard hides after send button is clicked
        /*try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }*/

        sendResponse();
    }

    public void sendResponse(){

        if(messages.get(counter + 1).equalsIgnoreCase("> I'm good thanks")) {
            response = "That's great, how about you answer some questions now?";
            messages.add(response);
            adapter.notifyDataSetChanged();
        }else if(messages.get(messages.size() - 1).equalsIgnoreCase("> Sure")){
            response = "Awesome, from a scale of 1 to 10 how well was the module taught overall?";
            messages.add(response);
            adapter.notifyDataSetChanged();
        }else if(messages.get(messages.size() - 1).equalsIgnoreCase("> 10")){
            response = "That's good to hear!";
            messages.add(response);
            adapter.notifyDataSetChanged();
        }else{
            response = "Sorry, could you rephrase that?";
            messages.add(response);
            adapter.notifyDataSetChanged();
        }
        counter++;
        if(counter > 1){
            messages.remove(0);
            messages.remove(1);
            adapter.notifyDataSetChanged();
        }
        botMessage = new Message(activeModule, "Bot said: " + response);
        String responseId = botMessage.getId();
        myRef.child("messages").child(responseId).setValue(botMessage);
    }

}
