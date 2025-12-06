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
import com.example.newsapplication.model.User;
import com.example.newsapplication.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private Context context;
    private List<User> users;
    private UserActionListener listener;

    public interface UserActionListener {
        void onChangeRole(User user);
        void onBanUser(User user);
        void onUnbanUser(User user);
    }

    public AdminUsersAdapter(Context context, List<User> users, UserActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        
        holder.userName.setText(user.getDisplayName() != null && !user.getDisplayName().isEmpty() 
            ? user.getDisplayName() 
            : "User");
        
        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            email = "No email";
        }
        holder.userEmail.setText(email);
        
        String role = user.getRole();
        holder.roleBadge.setText(role.toUpperCase());
        
        if ("admin".equals(role)) {
            holder.roleBadge.setBackgroundResource(R.drawable.badge_admin);
        } else if ("author".equals(role)) {
            holder.roleBadge.setBackgroundResource(R.drawable.badge_author);
        } else {
            holder.roleBadge.setBackgroundResource(R.drawable.badge_reader);
        }
        
        if (user.isBanned()) {
            holder.banStatus.setVisibility(View.VISIBLE);
        } else {
            holder.banStatus.setVisibility(View.GONE);
        }
        
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Picasso.get()
                .load(user.getAvatarUrl())
                .transform(new CircleTransform())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.userAvatar);
        } else {
            holder.userAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        holder.userCard.setOnClickListener(v -> showActionsDialog(user));
    }

    private void showActionsDialog(User user) {
        String[] actions = user.isBanned() 
            ? new String[]{"Change Role", "Unban User"}
            : new String[]{"Change Role", "Ban User"};
        
        new android.app.AlertDialog.Builder(context)
            .setTitle("User Actions")
            .setItems(actions, (dialog, which) -> {
                if (which == 0) {
                    listener.onChangeRole(user);
                } else {
                    if (user.isBanned()) {
                        listener.onUnbanUser(user);
                    } else {
                        listener.onBanUser(user);
                    }
                }
            })
            .show();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CardView userCard;
        ImageView userAvatar;
        TextView userName;
        TextView userEmail;
        TextView roleBadge;
        TextView banStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userCard = itemView.findViewById(R.id.userCard);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            roleBadge = itemView.findViewById(R.id.roleBadge);
            banStatus = itemView.findViewById(R.id.banStatus);
        }
    }
}
