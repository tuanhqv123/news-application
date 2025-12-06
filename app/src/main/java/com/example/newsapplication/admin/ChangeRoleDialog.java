package com.example.newsapplication.admin;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.newsapplication.R;
import com.example.newsapplication.model.User;
import com.example.newsapplication.model.UserRole;
import com.google.android.material.button.MaterialButton;

public class ChangeRoleDialog extends Dialog {

    private User user;
    private RoleChangeListener listener;
    private RadioGroup roleRadioGroup;
    private RadioButton radioAdmin;
    private RadioButton radioAuthor;
    private RadioButton radioReader;

    public interface RoleChangeListener {
        void onRoleChanged(User user, UserRole newRole);
    }

    public ChangeRoleDialog(@NonNull Context context, User user, RoleChangeListener listener) {
        super(context);
        this.user = user;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_change_role);

        TextView title = findViewById(R.id.dialogTitle);
        title.setText("Change Role for " + user.getDisplayName());

        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        radioAdmin = findViewById(R.id.radioAdmin);
        radioAuthor = findViewById(R.id.radioAuthor);
        radioReader = findViewById(R.id.radioReader);

        // Set current role
        String currentRole = user.getRole();
        if ("admin".equals(currentRole)) {
            radioAdmin.setChecked(true);
        } else if ("author".equals(currentRole)) {
            radioAuthor.setChecked(true);
        } else {
            radioReader.setChecked(true);
        }

        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            int selectedId = roleRadioGroup.getCheckedRadioButtonId();
            UserRole newRole;

            if (selectedId == R.id.radioAdmin) {
                newRole = UserRole.ADMIN;
            } else if (selectedId == R.id.radioAuthor) {
                newRole = UserRole.AUTHOR;
            } else {
                newRole = UserRole.READER;
            }

            listener.onRoleChanged(user, newRole);
            dismiss();
        });
    }
}
