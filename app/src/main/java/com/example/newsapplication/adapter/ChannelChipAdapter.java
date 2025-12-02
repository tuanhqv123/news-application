package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Channel;

import java.util.ArrayList;
import java.util.List;

// Adapter for displaying channels as horizontal chips
public class ChannelChipAdapter extends RecyclerView.Adapter<ChannelChipAdapter.ChipViewHolder> {

    private List<Channel> channels;
    private OnChannelClickListener listener;
    private int selectedPosition = -1;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel, int position);
    }

    public ChannelChipAdapter(List<Channel> channels, OnChannelClickListener listener) {
        this.channels = channels != null ? channels : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channel_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        Channel channel = channels.get(position);
        holder.bind(channel, position == selectedPosition, listener, position);
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels != null ? channels : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition >= 0) {
            notifyItemChanged(selectedPosition);
        }
    }

    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = -1;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;

        public ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.channelChipName);
        }

        public void bind(Channel channel, boolean isSelected, OnChannelClickListener listener, int position) {
            nameTextView.setText(channel.getName());
            
            // Update selection state
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.category_chip_selected);
                nameTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.white));
            } else {
                itemView.setBackgroundResource(R.drawable.category_chip_background);
                nameTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChannelClick(channel, position);
                }
            });
        }
    }
}
