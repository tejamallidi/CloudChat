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
import com.dev.surya.cloudchat.MessageActivity;
import com.dev.surya.cloudchat.Model.Message;
import com.dev.surya.cloudchat.Model.User;
import com.dev.surya.cloudchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_SENDER = 1;
    public static final int MSG_TYPE_RECEIVER = 0;

    private Context mContext;
    private List<Message> mChat;

    private FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Message> mChat) {
        this.mContext = mContext;
        this.mChat = mChat;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == MSG_TYPE_SENDER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_sender, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_receiver, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int i) {
        Message message = mChat.get(i);
        holder.sendMessage.setText(message.getMessage());
        if(i == mChat.size() - 1){
            if(message.isIsseen()){
                holder.msgSeen.setText("seen");
            } else {
                holder.msgSeen.setText("Delivered");
            }
        } else {
            holder.msgSeen.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView sendMessage;
        public TextView msgSeen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            sendMessage = itemView.findViewById(R.id.textSend);
            msgSeen = itemView.findViewById(R.id.msg_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_SENDER;
        } else {
            return MSG_TYPE_RECEIVER;
        }
    }
}

