package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.GET_ORDERS_BY_DRIVER;
import static com.example.prif233.Utils.Constants.GET_ORDERS_BY_USER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.LocalDateAdapter;
import com.example.prif233.Utils.LocalDateTimeAdapter;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.FoodOrder;
import com.example.prif233.model.OrderStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyOrders extends AppCompatActivity {

    private int userId;
    private boolean isDriver;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", 0);
        isDriver = intent.getBooleanExtra("isDriver", false);

        // FIXED: Initialize Gson with proper date adapters
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gson = builder.create();

        if (isDriver) {
            setTitle("Delivery History");
        } else {
            setTitle("Order History");
        }

        loadOrders();
    }

    private void loadOrders() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String url = isDriver ? GET_ORDERS_BY_DRIVER + userId : GET_ORDERS_BY_USER + userId;

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(url);
                handler.post(() -> {
                    try {
                        if (response != null && !response.equals("Error")) {
                            Type ordersListType = new TypeToken<List<FoodOrder>>() {}.getType();
                            List<FoodOrder> ordersListFromJson = gson.fromJson(response, ordersListType);

                            ListView ordersListElement = findViewById(R.id.myOrderList);
                            MyOrdersAdapter adapter = new MyOrdersAdapter(this, ordersListFromJson);
                            ordersListElement.setAdapter(adapter);

                            ordersListElement.setOnItemClickListener((parent, view, position, id) -> {
                                FoodOrder selectedOrder = ordersListFromJson.get(position);

                                // If it's a driver and the order is not yet completed, go to active delivery screen
                                if (isDriver && selectedOrder.getOrderStatus() != OrderStatus.COMPLETED &&
                                        selectedOrder.getOrderStatus() != OrderStatus.CANCELLED) {
                                    Intent intentDelivery = new Intent(MyOrders.this, ActiveDeliveryActivity.class);
                                    // FIXED: Use the same gson instance to serialize
                                    intentDelivery.putExtra("orderJson", gson.toJson(selectedOrder));
                                    startActivity(intentDelivery);
                                } else {
                                    Intent intentDetails = new Intent(MyOrders.this, OrderDetailsActivity.class);
                                    // FIXED: Use the same gson instance to serialize
                                    intentDetails.putExtra("orderJson", gson.toJson(selectedOrder));
                                    intentDetails.putExtra("userId", userId);
                                    startActivity(intentDetails);
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}