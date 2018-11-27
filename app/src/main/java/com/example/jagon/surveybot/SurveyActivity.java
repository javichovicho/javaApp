package com.example.jagon.surveybot;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.util.ChatBot;
import com.github.bassaer.chatmessageview.view.ChatView;
import com.github.bassaer.chatmessageview.view.MessageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import ai.api.AIListener;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class SurveyActivity extends AppCompatActivity implements AIListener {

    private String activeModule = "";
    private MessageView mChatView;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseDatabase database;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private DatabaseReference messageDataReference;
    private final String DATA_PATH = "data";
    private String userId;
    private TextView userName;

    private AIService aiService;

    private final Client me = new Client();
    private final Client you = new Client();

    private Date date;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
    private com.example.jagon.surveybot.Message message;
    private String messageId;

    private com.example.jagon.surveybot.Message botMessage;
    private static final String BOT_ID = "Bot";

    private com.example.jagon.surveybot.Message retrievedMessage;
    private DatabaseReference individualMessagesReference;
    private final String MESSAGE_PATH = "data/messages";
    private String temporaryUserId = "";
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        intent = getIntent();
        activeModule = intent.getStringExtra("moduleName");
        setTitle(activeModule);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userId = firebaseUser.getUid();

        database = FirebaseDatabase.getInstance();
        messageDataReference = database.getReference(DATA_PATH);


        requestAudioPermissions();

        final AIConfiguration config = new AIConfiguration("45c92c4edf424d9ba2a23588beacb570",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        int myId = 0;
        String myName = "User";
        int yourId = 1;
        String yourName = "Bot";
        me.setName(myName);
        me.setId(myId);
        you.setName(yourName);
        you.setId(yourId);

        mChatView = (MessageView)findViewById(R.id.my_chat_view);

        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.green500));
        mChatView.setLeftBubbleColor(Color.WHITE);
        mChatView.setBackgroundColor(ContextCompat.getColor(this, R.color.blueGray500));
        mChatView.setRightMessageTextColor(Color.WHITE);
        mChatView.setLeftMessageTextColor(Color.BLACK);
        mChatView.setUsernameTextColor(Color.WHITE);
        mChatView.setSendTimeTextColor(Color.WHITE);
        mChatView.setMessageMarginTop(5);
        mChatView.setMessageMarginBottom(5);

        userName = (TextView)findViewById(R.id.hiddenTextView);

        // Firebase components
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
                    userName.setText(user.getName());
                    me.setName(user.getName());
                }catch(NullPointerException e){
                    Log.i("Exception error", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SurveyActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });
        loadOldConversation();
    }

    public void buttonListenOnClick(final View view) {
        aiService.startListening();
    }
    public void onResult(final AIResponse response) {
        final Result result = response.getResult();
        final String userTextMessage = result.getResolvedQuery();
        final String botTextMessage = result.getFulfillment().getSpeech();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        date = new Date();
        message = new com.example.jagon.surveybot.Message(activeModule, userTextMessage, userId, dateFormat.format(date));
        messageId = message.getId();
        messageDataReference.child("messages").child(messageId).setValue(message);

        com.github.bassaer.chatmessageview.model.Message message = new com.github.bassaer.chatmessageview.model.Message.Builder()
                .setUser(me)
                .setRight(true)
                .setText(result.getResolvedQuery())
                .hideIcon(true)
                .build();

        //Set to chat view
        mChatView.setMessage(message);
        mChatView.scrollToEnd();

        final com.github.bassaer.chatmessageview.model.Message receivedMessage = new Message.Builder()
                .setUser(you)
                .setRight(false)
                .setText(result.getFulfillment().getSpeech())
                .build();

        // This is a demo bot
        // Return within 3 seconds
        int sendDelay = (new Random().nextInt(4) + 1) * 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mChatView.setMessage(receivedMessage);
                date = new Date();
                botMessage = new com.example.jagon.surveybot.Message(activeModule,
                        botTextMessage, BOT_ID, dateFormat.format(date));
                messageDataReference.child("messages").child(botMessage.getId()).setValue(botMessage);
                mChatView.scrollToEnd();
            }
        }, sendDelay);

    }
    @Override
    public void onError(final AIError error) {
        // print error toast
    }
    @Override
    public void onListeningStarted() {}

    @Override
    public void onListeningCanceled() {}

    @Override
    public void onListeningFinished() {}

    @Override
    public void onAudioLevel(final float level) {}

    //Requesting run-time permissions

    //Create placeholder for user's consent to record_audio permission.
    //This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            // recordAudio();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // recordAudio();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void loadOldConversation(){
        retrievedMessage = new com.example.jagon.surveybot.Message();
        individualMessagesReference = database.getReference(MESSAGE_PATH);
        individualMessagesReference.orderByChild("timeStamp").
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    retrievedMessage = snapshot.getValue(com.example.jagon.surveybot.Message.class);
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

                        String strDate = retrievedMessage.getTimeStamp();
                        Date date = null;
                        try {
                            date = dateFormat.parse(strDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        if(temporaryUserId.equals(userId)){
                            final com.github.bassaer.chatmessageview.model.Message tempMessage = new Message.Builder()
                                    .setUser(me)
                                    .setRight(true)
                                    .setText(temp)
                                    .setSendTime(cal)
                                    .build();
                            mChatView.setMessage(tempMessage);
                        }else {
                            final com.github.bassaer.chatmessageview.model.Message tempMessage = new Message.Builder()
                                    .setUser(you)
                                    .setRight(false)
                                    .setText(temp)
                                    .setSendTime(cal)
                                    .build();
                            mChatView.setMessage(tempMessage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SurveyActivity.this, databaseError.getCode(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
