package com.dev.surya.cloudchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, currentState, senderUserID;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineRequestButton,sendMessageButton;

    private FirebaseAuth mAuth;

    private DatabaseReference usersRef, chatRequestRef, contactsRef, notificationRef,chatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Chatlist");

        receiverUserID = getIntent().getExtras().get("userid").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineRequestButton = findViewById(R.id.decline_message_request_button);
        sendMessageButton = findViewById(R.id.send_message_button);
        currentState = "new";

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {
        usersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("imageURL"))){
                    String userImage = dataSnapshot.child("imageURL").getValue().toString();
                    String userName = dataSnapshot.child("username").getValue().toString();
                    String userStatus = dataSnapshot.child("userstatus").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();

                } else {
                    String userName = dataSnapshot.child("username").getValue().toString();
                    String userStatus = dataSnapshot.child("userstatus").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiverUserID)){
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if(requestType.equals("sent")){
                        currentState = "request_sent";
                        sendMessageRequestButton.setText("Decline Chat Request");
                    } else if(requestType.equals("received")){
                        currentState = "request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");
                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setEnabled(true);
                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) { declineChatRequest();
                            }
                        });
                    }
                }
                else {
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserID)){
                                currentState = "friends";
                                sendMessageRequestButton.setText("Delete this Contact");
                                sendMessageButton.setVisibility(View.VISIBLE);
                                sendMessageButton.setText("Send Message");
                                sendMessageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ProfileActivity.this, MessageActivity.class);
                                        intent.putExtra("userid", receiverUserID);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!senderUserID.equals(receiverUserID)){

            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if(currentState.equals("new")){
                        sendChatRequest();
                    }
                    if(currentState.equals("request_sent")){
                        declineChatRequest();
                    }
                    if(currentState.equals("request_received")){
                        acceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        removeSpecificContact();
                    }
                }
            });

        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = "new";
                                                removeChat();
                                                sendMessageRequestButton.setText("Send Message Request");
                                                sendMessageButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    sendMessageButton.setVisibility(View.VISIBLE);
                                                                                    sendMessageRequestButton.setText("Delete this Contact");
                                                                                    declineRequestButton.setVisibility(View.INVISIBLE);
                                                                                    declineRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void declineChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestButton.setText("Send Message");
                                                sendMessageButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {

        chatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            sendMessageRequestButton.setEnabled(true);
                                                            currentState = "request_sent";
                                                            sendMessageRequestButton.setText("Decline Chat Request");
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void removeChat(){
        final DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference("Chatlist");
        chatRef.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, "Contact Deleted Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
