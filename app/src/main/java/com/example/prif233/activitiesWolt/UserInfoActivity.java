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

        // Initialize views
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

        // Get user info from intent
        String userInfo = getIntent().getStringExtra("userJsonObject");
        if (userInfo != null) {
            loadUserInfo(userInfo);

            // Refresh user data from server to get latest loyalty points
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

            // Parse as JsonObject to check user type
            JsonObject userJson = gson.fromJson(userInfo, JsonObject.class);
            userType = userJson.has("userType") ? userJson.get("userType").getAsString() : "";
            userId = userJson.has("id") ? userJson.get("id").getAsInt() : 0;

            // Populate common fields
            if (userJson.has("login")) {
                loginField.setText(userJson.get("login").getAsString());
            }
            if (userJson.has("password")) {
                passwordField.setText("••••••••"); // Don't show actual password
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

            // Check if user is BasicUser (has address and loyalty points)
            if (userType.contains("BasicUser") || userType.contains("Driver")) {
                isBasicUser = true;

                // Show address field
                if (userJson.has("address")) {
                    String address = userJson.get("address").getAsString();
                    addressField.setText(address);
                    addressField.setVisibility(View.VISIBLE);
                    findViewById(R.id.labelAddress).setVisibility(View.VISIBLE);
                }

                // Show loyalty points card
                loyaltyPointsCard.setVisibility(View.VISIBLE);

                // Display loyalty points (will be updated from server)
                if (userJson.has("loyaltyPoints")) {
                    int loyaltyPoints = userJson.get("loyaltyPoints").getAsInt();
                    displayLoyaltyPoints(loyaltyPoints);
                } else {
                    displayLoyaltyPoints(0);
                }
            } else {
                // Hide address and loyalty points for non-BasicUser
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

    /**
     * Refresh user data from server to get latest loyalty points
     */
    private void refreshUserData() {
        loadingIndicator.setVisibility(View.VISIBLE);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Fetch fresh user data from server
                String url = Constants.GET_USER_BY_ID_URL + userId;
                String response = RestOperations.sendGet(url);

                handler.post(() -> {
                    loadingIndicator.setVisibility(View.GONE);

                    if (response != null && !response.equals("Error")) {
                        try {
                            Gson gson = new Gson();
                            JsonObject userJson = gson.fromJson(response, JsonObject.class);

                            // Update loyalty points with fresh data
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
        // Main points display
        String pointsText = "🎁 " + points + " Points";
        loyaltyPointsTextView.setText(pointsText);

        // Calculate approximate value (assuming 1 point = €0.10 discount)
        double pointsValue = points * 0.10;
        String valueText;

        if (points == 0) {
            valueText = "Start earning points with your orders!";
        } else if (points < 10) {
            valueText = String.format("Keep ordering to earn more! (≈ €%.2f value)", pointsValue);
        } else {
            valueText = String.format("Great job! You have ≈ €%.2f in rewards", pointsValue);
        }

        loyaltyPointsValueText.setText(valueText);
    }

    /**
     * Refresh button click handler
     */
    public void refreshLoyaltyPoints(View view) {
        if (isBasicUser && userId > 0) {
            refreshUserData();
            Toast.makeText(this, "Refreshing loyalty points...", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateUserInfo(View view) {
        // Here you would typically send a PUT request to your backend
        // For now, we'll just show a toast
        Toast.makeText(this, "Information updated (simulation)", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh loyalty points when returning to this activity
        if (isBasicUser && userId > 0) {
            refreshUserData();
        }
    }
}