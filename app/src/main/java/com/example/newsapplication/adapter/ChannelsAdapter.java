package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Channel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

// Adapter for displaying channels in a RecyclerView
public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {

    private List<Channel> channels;
    private OnChannelClickListener listener;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
        void onFollowClick(Channel channel, int position);
    }

    public ChannelsAdapter() {
        this.channels = new ArrayList<>();
    }

    public ChannelsAdapter(List<Channel> channels, OnChannelClickListener listener) {
        this.channels = channels != null ? channels : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channels.get(position);
        holder.bind(channel, listener, position);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels != null ? channels : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setListener(OnChannelClickListener listener) {
        this.listener = listener;
    }

    public void updateChannelFollowStatus(int position, boolean isFollowing) {
        if (position >= 0 && position < channels.size()) {
            channels.get(position).setFollowing(isFollowing);
            notifyItemChanged(position);
        }
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        private final ImageView logoImageView;
        private final TextView nameTextView;
        private final TextView descriptionTextView;
        private final Button followButton;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            logoImageView = itemView.findViewById(R.id.channelLogoImageView);
            nameTextView = itemView.findViewById(R.id.channelNameTextView);
            descriptionTextView = itemView.findViewById(R.id.channelDescriptionTextView);
            followButton = itemView.findViewById(R.id.followButton);
        }

        public void bind(Channel channel, OnChannelClickListener listener, int position) {
            // Set channel name
            nameTextView.setText(channel.getName());
            
            // Set description - handle null and "null" string
            String description = channel.getDescription();
            if (description != null && !description.isEmpty() && !description.equals("null")) {
                descriptionTextView.setText(description);
                descriptionTextView.setVisibility(View.VISIBLE);
            } else {
                descriptionTextView.setText("News channel");
                descriptionTextView.setVisibility(View.VISIBLE);
            }
            
            // Load logo
            String logoUrl = channel.getLogoUrl();
            if (logoUrl != null && !logoUrl.isEmpty() && !logoUrl.equals("null")) {
                Picasso.get()
                        .load(logoUrl)
                        .noPlaceholder()
                        .error(R.drawable.default_avatar)
                        .into(logoImageView);
            } else {
                logoImageView.setImageResource(R.drawable.default_avatar);
            }
            
            // Set follow button state
            updateFollowButton(channel.isFollowing());
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChannelClick(channel);
                }
            });
            
            followButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFollowClick(channel, position);
                }
            });
        }

        private void updateFollowButton(boolean isFollowing) {
            if (isFollowing) {
                followButton.setText("Following");
                followButton.setBackgroundResource(R.drawable.button_following_background);
                followButton.setTextColor(android.graphics.Color.WHITE);
            } else {
                followButton.setText("Follow");
                followButton.setBackgroundResource(R.drawable.button_follow_background);
                followButton.setTextColor(android.graphics.Color.WHITE);
            }
        }
    }
}
