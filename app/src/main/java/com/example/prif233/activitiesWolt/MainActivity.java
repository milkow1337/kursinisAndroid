package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.VALIDATE_USER_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.RestOperations;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void validateUser(View view) {
        TextView login = findViewById(R.id.loginField);
        TextView password = findViewById(R.id.passwordField);

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("login", login.getText().toString());
        jsonObject.addProperty("password", password.getText().toString());
        String info = gson.toJson(jsonObject);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(VALIDATE_USER_URL, info);
                handler.post(() -> {
                    if (response != null && !response.equals("Error") && !response.isEmpty()) {
                        try {
                            JsonObject userResponse = gson.fromJson(response, JsonObject.class);

                            // Get the userType field from the backend response
                            String userType = "";
                            if (userResponse.has("userType")) {
                                userType = userResponse.get("userType").getAsString();
                            }

                            System.out.println("User type from server: " + userType);

                            // Check if this is a Restaurant user - they should use desktop app
                            if (userType.contains("Restaurant")) {
                                Toast.makeText(MainActivity.this,
                                        "Restaurants must use the desktop application. Mobile app is for customers and drivers only.",
                                        Toast.LENGTH_LONG).show();
                            } else if (userType.contains("Driver") || userType.contains("BasicUser") || userType.contains("User")) {
                                // Allow BasicUser (customers) and Drivers to proceed
                                Intent intent = new Intent(MainActivity.this, WoltRestaurants.class);
                                intent.putExtra("userJsonObject", response);
                                startActivity(intent);
                            } else {
                                // Unknown user type
                                Toast.makeText(MainActivity.this,
                                        "Invalid user type. Please contact support.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,
                                    "Error processing login response: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Invalid credentials. Please check your username and password.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() ->
                        Toast.makeText(MainActivity.this,
                                "Network error. Please check your connection and server status.",
                                Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    public void loadRegWindow(View view) {
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }
}