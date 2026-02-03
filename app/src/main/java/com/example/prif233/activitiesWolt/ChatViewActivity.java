package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.GET_MESSAGES_BY_ORDER;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatViewActivity extends AppCompatActivity {

    private int orderId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        orderId = getIntent().getIntExtra("orderId", 0);
        userId = getIntent().getIntExtra("userId", 0);

        setTitle("Order Chat");
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

                            ListView messagesListElement = findViewById(R.id.chatMessageList);

                            if (messagesListFromJson == null || messagesListFromJson.isEmpty()) {
                                Toast.makeText(this, "No messages yet", Toast.LENGTH_SHORT).show();
                            } else {
                                ArrayAdapter<Review> adapter = new ArrayAdapter<>(this,
                                        android.R.layout.simple_list_item_1, messagesListFromJson);
                                messagesListElement.setAdapter(adapter);
                            }
                        } else {
                            Toast.makeText(this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh messages when returning to activity
        loadMessages();
    }
}