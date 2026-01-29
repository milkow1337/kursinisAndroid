package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.GET_MESSAGES_BY_ORDER;
import static com.example.prif233.Utils.Constants.SEND_MESSAGE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.LocalDateAdapter;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.Review;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatSystem extends AppCompatActivity {

    private int orderId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_system);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        orderId = intent.getIntExtra("orderId", 0);
        userId = intent.getIntExtra("userId", 0);

        loadMessages();
    }

    private void loadMessages() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_MESSAGES_BY_ORDER + orderId);
                handler.post(() -> {
                    try {
                        if (response != null && !response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
                            Gson gsonMessages = gsonBuilder.setPrettyPrinting().create();
                            Type messagesListType = new TypeToken<List<Review>>() {}.getType();
                            List<Review> messagesListFromJson = gsonMessages.fromJson(response, messagesListType);
                            
                            ListView messagesListElement = findViewById(R.id.messageList);
                            ArrayAdapter<Review> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messagesListFromJson);
                            messagesListElement.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(View view) {
        TextView messageBody = findViewById(R.id.bodyField);
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        int ratingValue = (int) ratingBar.getRating();
        String text = messageBody.getText().toString();

        if (text.isEmpty()) {
            Toast.makeText(this, "Please write a review text", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("userId", userId);
        jsonObject.addProperty("orderId", orderId);
        // Matching keys with the updated backend logic
        jsonObject.addProperty("messageText", text);
        jsonObject.addProperty("rating", ratingValue);

        String message = gson.toJson(jsonObject);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(SEND_MESSAGE, message);
                handler.post(() -> {
                    if (response != null && !response.equals("Error")) {
                        messageBody.setText("");
                        ratingBar.setRating(0);
                        loadMessages();
                        Toast.makeText(ChatSystem.this, "Review sent!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatSystem.this, "Failed to send review", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(ChatSystem.this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}