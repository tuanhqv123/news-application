package com.example.newsapplication.model.request;

import com.google.gson.annotations.SerializedName;

public class UserInvite {
    @SerializedName("email")
    private String email;

    @SerializedName("role_id")
    private int roleId;

    @SerializedName("channel_id")
    private Integer channelId;

    @SerializedName("invited_by")
    private String invitedBy;

    public UserInvite(String email, int roleId, String invitedBy) {
        this.email = email;
        this.roleId = roleId;
        this.invitedBy = invitedBy;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public Integer getChannelId() { return channelId; }
    public void setChannelId(Integer channelId) { this.channelId = channelId; }

    public String getInvitedBy() { return invitedBy; }
    public void setInvitedBy(String invitedBy) { this.invitedBy = invitedBy; }
}