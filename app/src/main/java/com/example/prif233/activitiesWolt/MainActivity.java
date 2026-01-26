package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.VALIDATE_USER_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

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
                    if (!response.equals("Error") && !response.isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, WoltRestaurants.class);
                        intent.putExtra("userJsonObject", response);
                        //??Jei noriu kazka is response paimt, man reikia parsint sia dali
                        //intent.putExtra("userId", )
                        startActivity(intent);
                    }
                });
            } catch (IOException e) {
                //Toast reikes
            }

        });

    }

    public void loadRegWindow(View view) {
        Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }
}