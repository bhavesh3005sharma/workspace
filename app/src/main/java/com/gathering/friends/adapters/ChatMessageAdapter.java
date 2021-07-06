package com.gathering.friends.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.LayoutChatMessagesBinding;
import com.gathering.friends.databinding.LayoutMessageReceivedBinding;
import com.gathering.friends.databinding.LayoutMessageSentBinding;
import com.gathering.friends.models.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter {

    List<ChatMessage> list;
    Context context;

    public ChatMessageAdapter(List<ChatMessage> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getSender_username().equals(Prefs.getUser(context).getUsername()))
            return 1;
        else
            return 2;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == 1) {
            LayoutMessageSentBinding layoutMessageSentBinding = LayoutMessageSentBinding.inflate(layoutInflater, parent, false);
            return new viewHolderSentMsg(layoutMessageSentBinding);
        } else if (viewType == 2) {
            LayoutMessageReceivedBinding layoutMessageReceivedBinding = LayoutMessageReceivedBinding.inflate(layoutInflater, parent, false);
            return new viewHolderReceivedMsg(layoutMessageReceivedBinding);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessages = list.get(position);
        if (isMyMessage(chatMessages.getSender_username())) {
            ((viewHolderSentMsg) holder).layoutMessageSentBinding.setChat(chatMessages);
        } else {
            ((viewHolderReceivedMsg) holder).layoutMessageReceivedBinding.setChat(chatMessages);
        }
    }

    private boolean isMyMessage(String username) {
        return username.equals(Prefs.getUser(context).getUsername());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class viewHolderSentMsg extends RecyclerView.ViewHolder {
        LayoutMessageSentBinding layoutMessageSentBinding;

        public viewHolderSentMsg(@NonNull LayoutMessageSentBinding layoutMessageSentBinding) {
            super(layoutMessageSentBinding.getRoot());
            this.layoutMessageSentBinding = layoutMessageSentBinding;
        }
    }

    public static class viewHolderReceivedMsg extends RecyclerView.ViewHolder {
       LayoutMessageReceivedBinding layoutMessageReceivedBinding;

        public viewHolderReceivedMsg(@NonNull LayoutMessageReceivedBinding layoutMessageReceivedBinding) {
            super(layoutMessageReceivedBinding.getRoot());
            this.layoutMessageReceivedBinding = layoutMessageReceivedBinding;
        }
    }
}
