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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ChatActivity extends AppCompatActivity {

    private String activeModule = "";
    private Intent intent;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String userId;
    private FirebaseDatabase database;
    private ListView chatListView;
    private ArrayList<String> messages;
    private ArrayAdapter adapter;
    private Message retrievedMessage;
    private Message message;
    private String messageId;
    private Message botMessage;
    private EditText chatEditText;
    private final int FULL_HEIGHT = 950;
    private final int MEDIUM_HEIGHT = 488;
    private DatabaseReference messageDataReference;
    private DatabaseReference individualMessagesReference;
    private final String DATA_PATH = "data";
    private final String MESSAGE_PATH = "data/messages";
    private final String BLANK = "";
    private static final String STATE_ITEMS = "messages";
    private static final String BOT_ID = "Bot";
    private String temporaryUserId = "";

    private int counter = 0;
    private String response = "";

    private boolean messageHistoryNotRetrieved = true;

    private Date date;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // The following prevents the keyboard from displaying automatically when activity starts
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        intent = getIntent();
        activeModule = intent.getStringExtra("moduleName");

        setTitle(activeModule);
        Log.i("Info", activeModule);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();

        database = FirebaseDatabase.getInstance();
        messageDataReference = database.getReference(DATA_PATH);

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
            retrievedMessage = new Message();
            individualMessagesReference = database.getReference(MESSAGE_PATH);
            // If addValueEventListener method was used it would update constantly
            // Another alternative is addChildEventListener(new ChildEventListener)
            individualMessagesReference.orderByChild("timeStamp").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        retrievedMessage = snapshot.getValue(Message.class);
                        try {
                            temporaryUserId = retrievedMessage.getUserId();
                        }catch(NullPointerException e){
                            Log.i("Exception", e.toString());
                        }
                        if((temporaryUserId.equals(userId) ||
                                retrievedMessage.getUserId().equals("Bot")) &&
                                retrievedMessage.getModuleName().equals(activeModule)){
                            Log.i("Message retrieved", retrievedMessage.getMessage());
                            String temp = retrievedMessage.getMessage();
                            messages.add(temp);
                        }
                        chatListView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChatActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
                }
            });
            messageHistoryNotRetrieved = false;
        }

        chatEditText = (EditText)findViewById(R.id.chatEditText);

        chatEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // ListView chatListView = (ListView)findViewById(R.id.chatListView);
                    ViewGroup.LayoutParams params = chatListView.getLayoutParams();
                    params.height = FULL_HEIGHT;
                    chatListView.setLayoutParams(params);
                    // The following scrolls the list view to the last message
                    scrollListView();
                }
                return false;
            }
        });
        chatEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ListView chatListView = (ListView)findViewById(R.id.chatListView);
                ViewGroup.LayoutParams params = chatListView.getLayoutParams();
                params.height = MEDIUM_HEIGHT;
                chatListView.setLayoutParams(params);
                scrollListView();
            }
        });

    }

    public void sendMessage(View view){
        // ListView chatListView = (ListView)findViewById(R.id.chatListView);
        // chatEditText = (EditText)findViewById(R.id.chatEditText);

        if(chatEditText.getText().toString().isEmpty()) {
            Toast.makeText(ChatActivity.this, "Message empty", Toast.LENGTH_SHORT).show();
        }else{
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            userId = user.getUid();

            date = new Date();
            message = new Message(activeModule, chatEditText.getText().toString(), userId, dateFormat.format(date));
            messages.add("> " + message.getMessage());

            adapter.notifyDataSetChanged();

            messageId = message.getId();

            messageDataReference.child("messages").child(messageId).setValue(message);

            Log.i("Message saved", message.toString());
            Toast.makeText(ChatActivity.this,  "Message sent", Toast.LENGTH_SHORT).show();
            chatEditText.setText(BLANK);

            sendResponse();
            scrollListView();
        }
    }

    public void sendResponse(){

        produceShortPause();
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
            adapter.notifyDataSetChanged();
        }
        date = new Date();
        botMessage = new Message(activeModule, response, BOT_ID, dateFormat.format(date));
        messageDataReference.child("messages").child(botMessage.getId()).setValue(botMessage);
    }

    private void scrollListView(){
        chatListView.setSelection(adapter.getCount() - 1);
    }
    private void produceShortPause(){
        try{
            TimeUnit.MILLISECONDS.sleep(250);
        }catch(InterruptedException e){
            Log.i("Exception", e.toString());
        }
    }

    //
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_ITEMS, messages);
    }

}
