package com.dev.surya.cloudchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dev.surya.cloudchat.Fragments.ChatsFragment;
import com.dev.surya.cloudchat.MessageActivity;
import com.dev.surya.cloudchat.Model.Message;
import com.dev.surya.cloudchat.Model.User;
import com.dev.surya.cloudchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isOnline;
    private String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isOnline) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isOnline = isOnline;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, viewGroup, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        final User user = mUsers.get(i);
        holder.username.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.drawable.profile_image);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }

        if(isOnline){
            lastMessage(user.getId(), holder.lastMessage);
        } else {
            holder.lastMessage.setVisibility(View.INVISIBLE);
        }

        if(isOnline){
            if(user.getStatus().equals("online")){
                holder.imageOnline.setVisibility(View.VISIBLE);
                holder.imageOffline.setVisibility(View.INVISIBLE);
            } else{
                holder.imageOnline.setVisibility(View.INVISIBLE);
                holder.imageOffline.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imageOnline.setVisibility(View.INVISIBLE);
            holder.imageOffline.setVisibility(View.INVISIBLE);
        }


        //For Message Activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
        // For Message Activity


    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView username, lastMessage, count;
        private ImageView profileImage;
        private ImageView imageOnline;
        private ImageView imageOffline;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profile_image);
            imageOnline = itemView.findViewById(R.id.img_online);
            imageOffline = itemView.findViewById(R.id.img_offline);
            lastMessage = itemView.findViewById(R.id.last_message);
            count = itemView.findViewById(R.id.unread_count);
        }
    }

    // check for last message
    private void lastMessage(final String userid, final TextView lastMessage) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        assert message != null;
                        if (message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userid) ||
                                message.getReceiver().equals(userid) && message.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = message.getMessage();
                        }

                    }
                    switch (theLastMessage) {
                        case "default":
                            lastMessage.setText("No Message yet");
                            break;

                        default:
                            lastMessage.setText(theLastMessage);
                            break;
                    }

                    theLastMessage = "default";
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
