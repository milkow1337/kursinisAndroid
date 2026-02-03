package com.example.prif233.activitiesWolt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.prif233.Utils.LocalDateTimeAdapter;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.Cuisine;
import com.example.prif233.model.FoodOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ActiveDeliveryActivity extends AppCompatActivity {

    private FoodOrder order;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_active_delivery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String orderJson = getIntent().getStringExtra("orderJson");
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        Gson gson = builder.create();
        order = gson.fromJson(orderJson, FoodOrder.class);

        TextView orderIdText = findViewById(R.id.deliveryOrderId);
        TextView customerName = findViewById(R.id.customerName);
        TextView customerAddress = findViewById(R.id.customerAddress);
        TextView customerPhone = findViewById(R.id.customerPhone);
        ListView itemsList = findViewById(R.id.deliveryItemsList);

        orderIdText.setText("Order ID: #" + order.getId());
        if (order.getBuyer() != null) {
            customerName.setText("Name: " + order.getBuyer().getName() + " " + order.getBuyer().getSurname());
            customerAddress.setText("Address: " + order.getBuyer().getAddress());
            phoneNumber = order.getBuyer().getPhoneNumber();
            customerPhone.setText("Phone: " + phoneNumber);
        }

        List<Cuisine> items = order.getCuisineList();
        if (items != null) {
            ArrayAdapter<Cuisine> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
            itemsList.setAdapter(adapter);
        }

        findViewById(R.id.btnCallCustomer).setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });
    }

    public void markAsDelivered(View view) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        JsonObject json = new JsonObject();
        json.addProperty("status", "COMPLETED");

        executor.execute(() -> {
            try {
                // Update status endpoint: updateOrderStatus/{orderId}
                String url = "http://192.168.50.103:8080/updateOrderStatus/" + order.getId();
                String response = RestOperations.sendPut(url, json.toString());
                handler.post(() -> {
                    if (response != null && !response.equals("Error")) {
                        Toast.makeText(this, "Delivery completed!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}