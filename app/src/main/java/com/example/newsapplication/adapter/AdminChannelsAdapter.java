package com.example.newsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.List;

public class AdminChannelsAdapter extends RecyclerView.Adapter<AdminChannelsAdapter.ChannelViewHolder> {

    private Context context;
    private List<JSONObject> channels;
    private ChannelActionListener listener;

    public interface ChannelActionListener {
        void onChannelClick(JSONObject channel);
    }

    public AdminChannelsAdapter(Context context, List<JSONObject> channels, ChannelActionListener listener) {
        this.context = context;
        this.channels = channels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        JSONObject channel = channels.get(position);

        holder.channelName.setText(channel.optString("name", "Unknown"));
        holder.channelDescription.setText(channel.optString("description", "No description"));

        boolean isActive = channel.optBoolean("is_active", true);
        holder.statusBadge.setText(isActive ? "ACTIVE" : "INACTIVE");
        holder.statusBadge.setBackgroundResource(isActive ? R.drawable.badge_author : R.drawable.badge_inactive);

        String logoUrl = channel.optString("logo_url", null);
        if (logoUrl != null && !logoUrl.isEmpty()) {
            Picasso.get()
                    .load(logoUrl)
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.channelLogo);
        } else {
            holder.channelLogo.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.channelCard.setOnClickListener(v -> listener.onChannelClick(channel));
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public void updateChannels(List<JSONObject> newChannels) {
        this.channels = newChannels;
        notifyDataSetChanged();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        CardView channelCard;
        ImageView channelLogo;
        TextView channelName;
        TextView channelDescription;
        TextView statusBadge;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            channelCard = itemView.findViewById(R.id.channelCard);
            channelLogo = itemView.findViewById(R.id.channelLogo);
            channelName = itemView.findViewById(R.id.channelName);
            channelDescription = itemView.findViewById(R.id.channelDescription);
            statusBadge = itemView.findViewById(R.id.statusBadge);
        }
    }
}
