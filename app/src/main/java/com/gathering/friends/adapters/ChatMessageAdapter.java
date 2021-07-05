package com.gathering.friends.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.databinding.LayoutChatMessagesBinding;
import com.gathering.friends.models.ChatMessage;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.viewHolder> {

    List<ChatMessage> list;
    Context context;

    public ChatMessageAdapter(List<ChatMessage> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LayoutChatMessagesBinding layoutChatMessagesBinding = LayoutChatMessagesBinding.inflate(layoutInflater, parent, false);
        return new viewHolder(layoutChatMessagesBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        ChatMessage message = list.get(position);
        holder.layoutChatMessagesBinding.setChat(message);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        LayoutChatMessagesBinding layoutChatMessagesBinding;

        public viewHolder(@NonNull LayoutChatMessagesBinding layoutChatMessagesBinding) {
            super(layoutChatMessagesBinding.getRoot());
            this.layoutChatMessagesBinding = layoutChatMessagesBinding;
        }
    }
}
