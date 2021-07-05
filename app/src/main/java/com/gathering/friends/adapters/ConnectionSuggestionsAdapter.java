package com.gathering.friends.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gathering.friends.R;
import com.gathering.friends.database.Prefs;
import com.gathering.friends.databinding.LayoutConnectionSuggestionBinding;
import com.gathering.friends.models.User;
import com.gathering.friends.util.ConnectionRequestHandler;
import com.gathering.friends.util.Constants;

import java.util.List;

public class ConnectionSuggestionsAdapter extends RecyclerView.Adapter<ConnectionSuggestionsAdapter.viewHolder> {

    List<User> list;
    Context context;

    public ConnectionSuggestionsAdapter(List<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LayoutConnectionSuggestionBinding layoutConnectionSuggestionBinding = LayoutConnectionSuggestionBinding.inflate(layoutInflater, parent, false);
        return new viewHolder(layoutConnectionSuggestionBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);
        holder.layoutConnectionSuggestionBinding.setUser(user);

        if (user.getConnectionStatus().equals(Constants.UNKNOWN)) {
            holder.layoutConnectionSuggestionBinding.connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list.remove(user);
                    notifyDataSetChanged();
                    ConnectionRequestHandler.sendConnectionRequest(Prefs.getUser(context).getUsername(), user.getUsername());
                }
            });
        } else if (user.getConnectionStatus().equals(Constants.CONNECTED)) {
            holder.layoutConnectionSuggestionBinding.connect.setText(R.string.connected);
            holder.layoutConnectionSuggestionBinding.connect.setTextColor(context.getColor(R.color.green));
            holder.layoutConnectionSuggestionBinding.connect.setBackground(context.getDrawable(R.drawable.button_background_stroke_green));
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        LayoutConnectionSuggestionBinding layoutConnectionSuggestionBinding;

        public viewHolder(@NonNull LayoutConnectionSuggestionBinding layoutConnectionSuggestionBinding) {
            super(layoutConnectionSuggestionBinding.getRoot());
            this.layoutConnectionSuggestionBinding = layoutConnectionSuggestionBinding;
        }
    }
}
