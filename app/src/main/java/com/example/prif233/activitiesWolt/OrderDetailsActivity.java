package com.example.prif233.activitiesWolt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.LocalDateAdapter;
import com.example.prif233.Utils.LocalDateTimeAdapter;
import com.example.prif233.model.Cuisine;
import com.example.prif233.model.FoodOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailsActivity extends AppCompatActivity {

    private int orderId;
    private int userId;
    private FoodOrder order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String orderJson = getIntent().getStringExtra("orderJson");
        userId = getIntent().getIntExtra("userId", 0);

        if (orderJson == null) {
            Toast.makeText(this, "Error: No order data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            GsonBuilder build = new GsonBuilder();
            build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
            build.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
            Gson gson = build.create();
            order = gson.fromJson(orderJson, FoodOrder.class);
            
            if (order == null) {
                throw new Exception("Order parsing failed");
            }
            
            orderId = order.getId();

            TextView idText = findViewById(R.id.orderIdText);
            TextView statusText = findViewById(R.id.orderStatusText);
            TextView priceText = findViewById(R.id.orderPriceText);
            ListView itemsList = findViewById(R.id.orderDetailsItemsList);

            idText.setText("Order ID: #" + order.getId());
            statusText.setText("Status: " + order.getOrderStatus());
            priceText.setText(String.format("Total Price: €%.2f", order.getPrice()));

            List<Cuisine> items = order.getCuisineList();
            if (items != null) {
                ArrayAdapter<Cuisine> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
                itemsList.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading order details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void openChat(View view) {
        Intent intentChat = new Intent(this, ChatSystem.class);
        intentChat.putExtra("orderId", orderId);
        intentChat.putExtra("userId", userId);
        startActivity(intentChat);
    }
}