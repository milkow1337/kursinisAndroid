package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.CREATE_ORDER;
import static com.example.prif233.Utils.Constants.GET_RESTAURANT_MENU;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.OnQuantityChangeListener {

    private int userId;
    private int restaurantId;
    private MenuAdapter menuAdapter;
    private TextView orderTotalTextView;
    private TextView orderItemsCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", 0);
        restaurantId = intent.getIntExtra("restaurantId", 0);

        orderTotalTextView = findViewById(R.id.orderTotal);
        orderItemsCountTextView = findViewById(R.id.orderItemsCount);

        loadMenu();
    }

    private void loadMenu() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_RESTAURANT_MENU + restaurantId);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
                            Gson gsonMenu = gsonBuilder.setPrettyPrinting().create();
                            Type menuListType = new TypeToken<List<Cuisine>>() {
                            }.getType();
                            List<Cuisine> menuListFromJson = gsonMenu.fromJson(response, menuListType);
                            
                            ListView menuListElement = findViewById(R.id.menuItems);
                            menuAdapter = new MenuAdapter(this, menuListFromJson);
                            menuListElement.setAdapter(menuAdapter);
                            
                            // Update order summary
                            updateOrderSummary();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateOrderSummary() {
        if (menuAdapter == null) return;

        Map<Integer, Integer> quantities = menuAdapter.getQuantities();
        List<Cuisine> menuItems = menuAdapter.getMenuItems();
        
        double total = 0.0;
        int itemCount = 0;

        for (Cuisine cuisine : menuItems) {
            int quantity = quantities.getOrDefault(cuisine.getId(), 0);
            if (quantity > 0) {
                total += cuisine.getPrice() * quantity;
                itemCount += quantity;
            }
        }

        orderTotalTextView.setText(String.format("Total: €%.2f", total));
        orderItemsCountTextView.setText(String.format("Items: %d", itemCount));
    }

    public void placeOrder(View view) {
        if (menuAdapter == null) {
            Toast.makeText(this, "Menu not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<Integer, Integer> quantities = menuAdapter.getQuantities();
        List<Cuisine> menuItems = menuAdapter.getMenuItems();

        boolean hasItems = false;
        for (int qty : quantities.values()) {
            if (qty > 0) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            Toast.makeText(this, "Please add items to your order", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        JsonObject orderJson = new JsonObject();
        orderJson.addProperty("userId", userId);
        orderJson.addProperty("restaurantId", restaurantId);

        JsonArray itemsArray = new JsonArray();
        for (Cuisine cuisine : menuItems) {
            int quantity = quantities.getOrDefault(cuisine.getId(), 0);
            if (quantity > 0) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("cuisineId", cuisine.getId());
                itemJson.addProperty("quantity", quantity);
                itemsArray.add(itemJson);
            }
        }
        orderJson.add("items", itemsArray);

        String orderData = gson.toJson(orderJson);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(CREATE_ORDER, orderData);
                System.out.println("Order response: " + response);
                handler.post(() -> {
                    if (!response.equals("Error") && !response.isEmpty()) {
                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        // Clear the cart
                        menuAdapter.getQuantities().clear();
                        menuAdapter.notifyDataSetChanged();
                        updateOrderSummary();
                        // Optionally go back to restaurants list
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateOrderSummary();
    }
}
