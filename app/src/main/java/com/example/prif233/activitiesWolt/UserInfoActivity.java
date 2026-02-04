package com.example.prif233.activitiesWolt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.Constants;
import com.example.prif233.Utils.LocalDateTimeAdapter;
import com.example.prif233.Utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserInfoActivity extends AppCompatActivity {

    private EditText loginField, passwordField, nameField, surnameField, phoneField, addressField;
    private TextView loyaltyPointsTextView, loyaltyPointsValueText;
    private ProgressBar loadingIndicator;
    private View loyaltyPointsCard;

    private int userId;
    private String userType;
    private boolean isBasicUser = false;

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
        addressField = findViewById(R.id.updateAddressField);
        loyaltyPointsTextView = findViewById(R.id.loyaltyPointsText);
        loyaltyPointsValueText = findViewById(R.id.loyaltyPointsValueText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        loyaltyPointsCard = findViewById(R.id.loyaltyPointsCard);

        String userInfo = getIntent().getStringExtra("userJsonObject");
        if (userInfo != null) {
            loadUserInfo(userInfo);

            if (isBasicUser && userId > 0) {
                refreshUserData();
            }
        }
    }

    private void loadUserInfo(String userInfo) {
        try {
            GsonBuilder build = new GsonBuilder();
            build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
            Gson gson = build.create();

            JsonObject userJson = gson.fromJson(userInfo, JsonObject.class);
            userType = userJson.has("userType") ? userJson.get("userType").getAsString() : "";
            userId = userJson.has("id") ? userJson.get("id").getAsInt() : 0;

            if (userJson.has("login")) {
                loginField.setText(userJson.get("login").getAsString());
            }
            if (userJson.has("password")) {
                passwordField.setText("••••••••");
            }
            if (userJson.has("name")) {
                nameField.setText(userJson.get("name").getAsString());
            }
            if (userJson.has("surname")) {
                surnameField.setText(userJson.get("surname").getAsString());
            }
            if (userJson.has("phoneNumber")) {
                phoneField.setText(userJson.get("phoneNumber").getAsString());
            }

            if (userType.contains("BasicUser") || userType.contains("Driver")) {
                isBasicUser = true;

                if (userJson.has("address")) {
                    String address = userJson.get("address").getAsString();
                    addressField.setText(address);
                    addressField.setVisibility(View.VISIBLE);
                    findViewById(R.id.labelAddress).setVisibility(View.VISIBLE);
                }

                loyaltyPointsCard.setVisibility(View.VISIBLE);

                if (userJson.has("loyaltyPoints")) {
                    int loyaltyPoints = userJson.get("loyaltyPoints").getAsInt();
                    displayLoyaltyPoints(loyaltyPoints);
                } else {
                    displayLoyaltyPoints(0);
                }
            } else {
                addressField.setVisibility(View.GONE);
                findViewById(R.id.labelAddress).setVisibility(View.GONE);
                loyaltyPointsCard.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading user info: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void refreshUserData() {
        loadingIndicator.setVisibility(View.VISIBLE);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String url = Constants.GET_USER_BY_ID_URL + userId;
                String response = RestOperations.sendGet(url);

                handler.post(() -> {
                    loadingIndicator.setVisibility(View.GONE);

                    if (response != null && !response.equals("Error")) {
                        try {
                            Gson gson = new Gson();
                            JsonObject userJson = gson.fromJson(response, JsonObject.class);

                            if (userJson.has("loyaltyPoints")) {
                                int loyaltyPoints = userJson.get("loyaltyPoints").getAsInt();
                                displayLoyaltyPoints(loyaltyPoints);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(this, "Could not refresh loyalty points",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void displayLoyaltyPoints(int points) {
        String pointsText = "🎁 " + points + " Points";
        loyaltyPointsTextView.setText(pointsText);

        double pointsValue = points * 0.10;
        String valueText;

        if (points == 0) {
            valueText = "Start earning points with your orders!";
        } else if (points < 10) {
            valueText = String.format("Keep ordering to earn more! (≈ €%.2f value)", pointsValue);
        } else {
            valueText = String.format("You have ≈ €%.2f in rewards", pointsValue);
        }

        loyaltyPointsValueText.setText(valueText);
    }

    public void refreshLoyaltyPoints(View view) {
        if (isBasicUser && userId > 0) {
            refreshUserData();
            Toast.makeText(this, "Refreshing loyalty points...", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateUserInfo(View view) {
        String name = nameField.getText().toString().trim();
        String surname = surnameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String address = addressField.getText().toString().trim();

        if (name.isEmpty() || surname.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObject updateData = new JsonObject();
        updateData.addProperty("name", name);
        updateData.addProperty("surname", surname);
        updateData.addProperty("phoneNumber", phone);

        if (isBasicUser && !address.isEmpty()) {
            updateData.addProperty("address", address);
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String updateJson = updateData.toString();

        executor.execute(() -> {
            try {
                String url = Constants.HOME_URL + "users/" + userId + "/profile";
                String response = RestOperations.sendPut(url, updateJson);

                handler.post(() -> {
                    if (response != null && !response.equals("Error")) {
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(this, "Network error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBasicUser && userId > 0) {
            refreshUserData();
        }
    }
}