package com.example.jagon.surveybot;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private String activeModule = "";

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String userId;
    private Message mensaje;

    private Message message;
    private Message botMessage;
    private EditText chatEditText;
    private ListView chatListView;

    private DatabaseReference conversationReference;
    private ArrayList<String> messages;

    private static final String STATE_ITEMS = "messages";

    private ArrayAdapter<String> adapter;

    private int counter = 0;
    private String response = "";

    private boolean messageHistoryNotRetrieved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Intent intent = getIntent();
        activeModule = intent.getStringExtra("moduleName");

        setTitle(activeModule);
        Log.i("Info", activeModule);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("data");

        chatListView = (ListView)findViewById(R.id.chatListView);

        if (savedInstanceState != null) {
            messages = (ArrayList<String>)savedInstanceState.getSerializable(STATE_ITEMS);
        }else{
            messages = new ArrayList<>();
            messages.add("Hello, welcome to the " + activeModule +
                    " module survey, how are you today?");
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(adapter);
        if(messageHistoryNotRetrieved){
            mensaje = new Message();
            conversationReference = database.getReference("data/messages");
            // If addValueEventListener method was used it would update constantly
            // Another alternative is addChildEventListener(new ChildEventListener)
            conversationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        mensaje = snapshot.getValue(Message.class);
                        if((mensaje.getUserId().equals(userId) || mensaje.getUserId().equals("Bot")) &&
                                mensaje.getModuleName().equals(activeModule)){
                            Log.i("Message retrieved", mensaje.getMessage());
                            String temp = mensaje.getMessage();
                            messages.add(temp);
                        }
                        chatListView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            messageHistoryNotRetrieved = false;
        }

        chatEditText = (EditText)findViewById(R.id.chatEditText);

        chatEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    ListView chatListView = (ListView)findViewById(R.id.chatListView);
                    ViewGroup.LayoutParams params = chatListView.getLayoutParams();
                    params.height = 950;
                    chatListView.setLayoutParams(params);
                    chatListView.setSelection(adapter.getCount() - 1);
                }
                return false;
            }
        });
        chatEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView chatListView = (ListView)findViewById(R.id.chatListView);
                ViewGroup.LayoutParams params = chatListView.getLayoutParams();
                params.height = 488;
                chatListView.setLayoutParams(params);
                chatListView.setSelection(adapter.getCount() - 1);
            }
        });

    }

    public void sendChat(View view){
        ListView chatListView = (ListView)findViewById(R.id.chatListView);

        chatEditText = (EditText)findViewById(R.id.chatEditText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        message = new Message(activeModule, chatEditText.getText().toString(), uid);
        messages.add("> " + message.getMessage());

        adapter.notifyDataSetChanged();

        String id = message.getId();

        myRef.child("messages").child(id).setValue(message);

        Log.i("Message saved", message.toString());
        Toast.makeText(ChatActivity.this,  "Message sent", Toast.LENGTH_SHORT).show();
        chatEditText.setText("");

        sendResponse();
        chatListView.setSelection(adapter.getCount() - 1);
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
            // messages.remove(0);
            // messages.remove(1);
            adapter.notifyDataSetChanged();
        }
        String botId = "Bot";
        botMessage = new Message(activeModule, response, botId);
        String responseId = botMessage.getId();
        myRef.child("messages").child(responseId).setValue(botMessage);
    }

    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_ITEMS, messages);
    }

}
