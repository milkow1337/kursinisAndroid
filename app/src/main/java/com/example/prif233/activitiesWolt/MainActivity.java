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
                        JsonObject userResponse = gson.fromJson(response, JsonObject.class);
                        
                        // Refined check: Only Driver has 'licence' or 'vehicleType' fields.
                        // BasicUser and Driver have 'address', while generic User might not.
                        // Restaurant also extends BasicUser but usually shouldn't have license info.
                        
                        boolean isDriver = userResponse.has("licence");
                        boolean isBasicUser = userResponse.has("address") && !isDriver;
                        
                        // If it has "address" but also potentially restaurant fields, it's a restaurant.
                        // Since your Restaurant class is empty and just extends BasicUser, 
                        // we need a way to distinguish them if the server doesn't provide a "type" field.
                        
                        // Checking if it's explicitly NOT a Driver or BasicUser by checking for Restaurant specific traits if any.
                        // Alternatively, check for a "type" or "dtype" field which GSON/Hibernate often includes.
                        
                        String userType = "";
                        if (userResponse.has("type")) {
                             userType = userResponse.get("type").getAsString();
                        }

                        if (userType.equalsIgnoreCase("RESTAURANT")) {
                            Toast.makeText(MainActivity.this, "Access denied: Restaurants cannot log in.", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(MainActivity.this, WoltRestaurants.class);
                            intent.putExtra("userJsonObject", response);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid credentials or server error.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(MainActivity.this, "Network error.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void loadRegWindow(View view) {
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }
}