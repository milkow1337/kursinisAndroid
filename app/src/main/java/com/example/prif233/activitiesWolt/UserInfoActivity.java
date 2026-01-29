package com.example.prif233.activitiesWolt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.LocalDateTimeAdapter;
import com.example.prif233.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;

public class UserInfoActivity extends AppCompatActivity {

    private EditText loginField, passwordField, nameField, surnameField, phoneField;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginField = findViewById(R.id.updateLoginField);
        passwordField = findViewById(R.id.updatePasswordField);
        nameField = findViewById(R.id.updateNameField);
        surnameField = findViewById(R.id.updateSurnameField);
        phoneField = findViewById(R.id.updatePhoneField);

        String userInfo = getIntent().getStringExtra("userJsonObject");
        if (userInfo != null) {
            GsonBuilder build = new GsonBuilder();
            build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
            Gson gson = build.create();
            currentUser = gson.fromJson(userInfo, User.class);

            loginField.setText(currentUser.getLogin());
            passwordField.setText(currentUser.getPassword());
            nameField.setText(currentUser.getName());
            surnameField.setText(currentUser.getSurname());
            phoneField.setText(currentUser.getPhoneNumber());
        }
    }

    public void updateUserInfo(View view) {
        // Here you would typically send a PUT request to your backend
        // For now, we'll just show a toast
        Toast.makeText(this, "Information updated (simulation)", Toast.LENGTH_SHORT).show();
        finish();
    }
}