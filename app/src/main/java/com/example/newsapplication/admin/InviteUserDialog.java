package com.example.newsapplication.admin;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.newsapplication.R;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.AuthEndpoints;
import com.example.newsapplication.api.endpoints.ChannelEndpoints;
import com.example.newsapplication.model.UserRole;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InviteUserDialog extends Dialog {

    private InviteListener listener;
    private EditText emailInput;
    private RadioGroup roleRadioGroup;
    private Spinner channelSpinner;
    private AuthEndpoints authEndpoints;
    private ChannelEndpoints channelEndpoints;
    private List<Channel> channels = new ArrayList<>();

    public interface InviteListener {
        void onUserInvited();
    }

    static class Channel {
        int id;
        String name;
        
        Channel(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    public InviteUserDialog(@NonNull Context context, InviteListener listener) {
        super(context);
        this.listener = listener;
        this.authEndpoints = new AuthEndpoints(context);
        this.channelEndpoints = new ChannelEndpoints(new ApiClient(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_invite_user);

        if (getWindow() != null) {
            getWindow().setLayout((int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.9), 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        emailInput = findViewById(R.id.emailInput);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        channelSpinner = findViewById(R.id.channelSpinner);

        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        MaterialButton btnInvite = findViewById(R.id.btnInvite);

        loadChannels();

        btnCancel.setOnClickListener(v -> dismiss());

        btnInvite.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = roleRadioGroup.getCheckedRadioButtonId();
            int roleId;

            if (selectedId == R.id.radioAdmin) {
                roleId = UserRole.ADMIN.getRoleId();
            } else if (selectedId == R.id.radioAuthor) {
                roleId = UserRole.AUTHOR.getRoleId();
            } else {
                roleId = UserRole.READER.getRoleId();
            }

            Integer channelId = null;
            if (channelSpinner.getSelectedItemPosition() > 0) {
                channelId = channels.get(channelSpinner.getSelectedItemPosition() - 1).id;
            }

            inviteUser(email, roleId, channelId);
        });
    }

    private void loadChannels() {
        channels.add(0, new Channel(0, "No Channel"));
        
        channelEndpoints.getPublicChannels(new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject fullResponse = response.getData();
                    JSONObject data = fullResponse.getJSONObject("data");
                    JSONArray channelsArray = data.getJSONArray("channels");
                    
                    for (int i = 0; i < channelsArray.length(); i++) {
                        JSONObject channel = channelsArray.getJSONObject(i);
                        channels.add(new Channel(
                            channel.getInt("id"),
                            channel.getString("name")
                        ));
                    }
                    
                    ArrayAdapter<Channel> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, channels);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    channelSpinner.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                // Ignore error, just show no channels
            }
        });
    }

    private void inviteUser(String email, int roleId, Integer channelId) {
        authEndpoints.inviteUser(email, roleId, channelId, new AuthEndpoints.InviteUserCallback() {
            @Override
            public void onSuccess(String userId, String userEmail, String role) {
                Toast.makeText(getContext(), "User invited successfully!", Toast.LENGTH_SHORT).show();
                listener.onUserInvited();
                dismiss();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
