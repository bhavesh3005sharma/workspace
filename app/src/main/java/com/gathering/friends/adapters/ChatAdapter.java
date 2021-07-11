package com.gathering.friends.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gathering.friends.R;
import com.gathering.friends.activities.DirectMessageActivity;
import com.gathering.friends.databinding.LayoutChatsBinding;
import com.gathering.friends.models.Room;
import com.gathering.friends.models.User;
import com.gathering.friends.util.Constants;

import java.util.List;

public class ChatAdapter<T> extends RecyclerView.Adapter<ChatAdapter.viewHolder> {

    List<T> list;
    Context context;
    String roomType;

    public ChatAdapter(List<T> list, Context context, String roomType) {
        this.list = list;
        this.context = context;
        this.roomType = roomType;
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
        User user = null;
        Room room = null;
        if (roomType.equals(Constants.DUO_ROOM)) {
            user = (User) list.get(position);
            if (user.getProfileUri() != null && !user.getProfileUri().isEmpty())
                Glide.with(holder.layoutChatsBinding.profileImage).load(user.getProfileUri()).placeholder(R.drawable.user).into(holder.layoutChatsBinding.profileImage);
            else holder.layoutChatsBinding.profileImage.setImageResource(R.drawable.user);
        } else {
            room = (Room) list.get(position);
            if (room.getPhotoUri() != null && !room.getPhotoUri().isEmpty())
                Glide.with(holder.layoutChatsBinding.profileImage).load(room.getPhotoUri()).placeholder(R.drawable.workspace_group).into(holder.layoutChatsBinding.profileImage);
            else {
                if (roomType.equals(Constants.GROUP_ROOM))
                    holder.layoutChatsBinding.profileImage.setImageResource(R.drawable.workspace_group);
                else
                    holder.layoutChatsBinding.profileImage.setImageResource(R.drawable.meetings);
            }
        }

        holder.layoutChatsBinding.setUser(user);
        holder.layoutChatsBinding.setRoom(room);

        User finalUser = user;
        Room finalRoom = room;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DirectMessageActivity.class);
                intent.putExtra("room_id", (roomType.equals(Constants.DUO_ROOM)) ? finalUser.getRoomId() : finalRoom.getRoomId());
                intent.putExtra("room_type", roomType);
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
