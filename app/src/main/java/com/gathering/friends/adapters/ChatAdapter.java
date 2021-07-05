package com.gathering.friends.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.activities.DirectMessageActivity;
import com.gathering.friends.databinding.LayoutChatsBinding;
import com.gathering.friends.models.User;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.viewHolder> {

    List<User> list;
    Context context;

    public ChatAdapter(List<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LayoutChatsBinding layoutChatsBinding = LayoutChatsBinding.inflate(layoutInflater, parent, false);
        return new viewHolder(layoutChatsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        holder.layoutChatsBinding.setUser(user);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DirectMessageActivity.class);
                intent.putExtra("room_id", user.getRoomId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        LayoutChatsBinding layoutChatsBinding;

        public viewHolder(@NonNull LayoutChatsBinding layoutChatsBinding) {
            super(layoutChatsBinding.getRoot());
            this.layoutChatsBinding = layoutChatsBinding;
        }
    }
}
