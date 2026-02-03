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

import androidx.appcompat.app.AppCompatActivity;

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
        setContentView(R.layout.activity_active_delivery);

        String orderJson = getIntent().getStringExtra("orderJson");

        if (orderJson == null || orderJson.isEmpty()) {
            Toast.makeText(this, "Error: No order data provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
            Gson gson = builder.create();
            order = gson.fromJson(orderJson, FoodOrder.class);

            if (order == null) {
                Toast.makeText(this, "Error: Failed to parse order data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            TextView orderIdText = findViewById(R.id.deliveryOrderId);
            TextView customerName = findViewById(R.id.customerName);
            TextView customerAddress = findViewById(R.id.customerAddress);
            TextView customerPhone = findViewById(R.id.customerPhone);
            ListView itemsList = findViewById(R.id.deliveryItemsList);

            if (orderIdText != null) {
                orderIdText.setText("Order ID: #" + order.getId());
            }

            if (order.getBuyer() != null) {
                String buyerName = "Name: ";
                if (order.getBuyer().getName() != null) {
                    buyerName += order.getBuyer().getName();
                }
                if (order.getBuyer().getSurname() != null) {
                    buyerName += " " + order.getBuyer().getSurname();
                }
                if (customerName != null) {
                    customerName.setText(buyerName);
                }

                String buyerAddress = "Address: ";
                if (order.getBuyer().getAddress() != null) {
                    buyerAddress += order.getBuyer().getAddress();
                } else {
                    buyerAddress += "Not provided";
                }
                if (customerAddress != null) {
                    customerAddress.setText(buyerAddress);
                }

                phoneNumber = order.getBuyer().getPhoneNumber();
                String phoneText = "Phone: ";
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    phoneText += phoneNumber;
                } else {
                    phoneText += "Not provided";
                }
                if (customerPhone != null) {
                    customerPhone.setText(phoneText);
                }
            } else {
                if (customerName != null) customerName.setText("Name: Not available");
                if (customerAddress != null) customerAddress.setText("Address: Not available");
                if (customerPhone != null) customerPhone.setText("Phone: Not available");
            }

            List<Cuisine> items = order.getCuisineList();
            if (items != null && !items.isEmpty() && itemsList != null) {
                ArrayAdapter<Cuisine> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
                itemsList.setAdapter(adapter);
            } else {
                Toast.makeText(this, "No items in this order", Toast.LENGTH_SHORT).show();
            }

            View callButton = findViewById(R.id.btnCallCustomer);
            if (callButton != null) {
                callButton.setOnClickListener(v -> {
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                    } else {
                        Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading delivery: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void markAsDelivered(View view) {
        if (order == null) {
            Toast.makeText(this, "Error: Order data not available", Toast.LENGTH_SHORT).show();
            return;
        }

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
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Network error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        });
    }
}