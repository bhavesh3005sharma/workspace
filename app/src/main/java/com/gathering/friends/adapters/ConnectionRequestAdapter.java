package com.gathering.friends.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.LayoutConnectionRequestBinding;
import com.gathering.friends.models.User;
import com.gathering.friends.util.ConnectionRequestHandler;
import com.gathering.friends.util.Constants;

import java.util.List;

public class ConnectionRequestAdapter extends RecyclerView.Adapter<ConnectionRequestAdapter.viewHolder> {

    List<User> list;
    Context context;
    String requestType;

    public ConnectionRequestAdapter(List<User> list, Context context, String requestType) {
        this.list = list;
        this.context = context;
        this.requestType = requestType;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LayoutConnectionRequestBinding layoutConnectionRequestBinding = LayoutConnectionRequestBinding.inflate(layoutInflater, parent, false);
        return new viewHolder(layoutConnectionRequestBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);

        // modify UI according to request type
        if (requestType.equals(Constants.REQUEST_SENT)) {
            holder.layoutConnectionRequestBinding.accept.setVisibility(View.GONE);
            holder.layoutConnectionRequestBinding.reject.setText(context.getString(R.string.withdraw));
        }

        holder.layoutConnectionRequestBinding.setUser(user);
        holder.layoutConnectionRequestBinding.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(user);
                notifyDataSetChanged();
                ConnectionRequestHandler.acceptRequest(Prefs.getUser(context).getUsername(), user.getUsername());
            }
        });
        holder.layoutConnectionRequestBinding.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.remove(user);
                notifyDataSetChanged();
                ConnectionRequestHandler.cancelRequest(Prefs.getUser(context).getUsername(), user.getUsername());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        LayoutConnectionRequestBinding layoutConnectionRequestBinding;

        public viewHolder(@NonNull LayoutConnectionRequestBinding layoutConnectionRequestBinding) {
            super(layoutConnectionRequestBinding.getRoot());
            this.layoutConnectionRequestBinding = layoutConnectionRequestBinding;
        }
    }
}
