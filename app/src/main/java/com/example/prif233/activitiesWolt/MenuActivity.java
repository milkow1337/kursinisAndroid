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
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.OnQuantityChangeListener {

    private int userId;
    private int restaurantId;
    private String restaurantName;
    private MenuAdapter menuAdapter;
    private TextView orderTotalTextView;
    private TextView orderItemsCountTextView;
    private View placeOrderButton;

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
        restaurantName = intent.getStringExtra("restaurantName");

        if (getSupportActionBar() != null && restaurantName != null) {
            getSupportActionBar().setTitle(restaurantName);
        }

        orderTotalTextView = findViewById(R.id.orderTotal);
        orderItemsCountTextView = findViewById(R.id.orderItemsCount);
        placeOrderButton = findViewById(R.id.btnPlaceOrder);

        loadMenu();
    }

    private void loadMenu() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        runOnUiThread(() -> {
            if (placeOrderButton != null) {
                placeOrderButton.setEnabled(false);
            }
        });

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_RESTAURANT_MENU + restaurantId);
                handler.post(() -> {
                    try {
                        if (response != null && !response.equals("Error") && !response.isEmpty()) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
                            Gson gsonMenu = gsonBuilder.setPrettyPrinting().create();
                            Type menuListType = new TypeToken<List<Cuisine>>() {}.getType();
                            List<Cuisine> menuListFromJson = gsonMenu.fromJson(response, menuListType);

                            if (menuListFromJson == null || menuListFromJson.isEmpty()) {
                                Toast.makeText(this, "No menu items available", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }

                            ListView menuListElement = findViewById(R.id.menuItems);
                            menuAdapter = new MenuAdapter(this, menuListFromJson);
                            menuListElement.setAdapter(menuAdapter);

                            if (placeOrderButton != null) {
                                placeOrderButton.setEnabled(true);
                            }

                            updateOrderSummary();
                        } else {
                            Toast.makeText(this, "Failed to load menu", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        finish();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> finish());
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

        final double finalTotal = total;
        final int finalItemCount = itemCount;

        runOnUiThread(() -> {
            orderTotalTextView.setText(String.format("Total: €%.2f", finalTotal));
            orderItemsCountTextView.setText(String.format("Items: %d", finalItemCount));
            if (placeOrderButton != null) {
                placeOrderButton.setEnabled(finalItemCount > 0);
            }
        });
    }

    public void placeOrder(View view) {
        if (menuAdapter == null) return;

        Map<Integer, Integer> quantities = menuAdapter.getQuantities();
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

        showOrderConfirmation(quantities, menuAdapter.getMenuItems());
    }

    private void showOrderConfirmation(Map<Integer, Integer> quantities, List<Cuisine> menuItems) {
        double total = 0.0;
        StringBuilder orderSummary = new StringBuilder();
        orderSummary.append("Order Summary:\n\n");

        for (Cuisine cuisine : menuItems) {
            int quantity = quantities.getOrDefault(cuisine.getId(), 0);
            if (quantity > 0) {
                total += cuisine.getPrice() * quantity;
                orderSummary.append(String.format("%dx %s - €%.2f\n",
                        quantity, cuisine.getName(), cuisine.getPrice() * quantity));
            }
        }

        orderSummary.append(String.format("\nTotal: €%.2f", total));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Order");
        builder.setMessage(orderSummary.toString());
        builder.setPositiveButton("Place Order", (dialog, which) -> {
            submitOrder(quantities, menuItems);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void submitOrder(Map<Integer, Integer> quantities, List<Cuisine> menuItems) {
        if (placeOrderButton != null) {
            placeOrderButton.setEnabled(false);
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
                handler.post(() -> {
                    // Refined success check: Backend returns the order object as JSON
                    if (response != null && !response.equals("Error") && response.contains("\"id\":")) {
                        
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Success!");
                        builder.setMessage("Your order has been placed successfully.");
                        builder.setPositiveButton("View My Orders", (dialog, which) -> {
                            Intent intent = new Intent(this, MyOrders.class);
                            intent.putExtra("id", userId);
                            startActivity(intent);
                            finish();
                        });
                        builder.setNegativeButton("Close", (dialog, which) -> finish());
                        builder.setCancelable(false);
                        builder.show();

                    } else {
                        Toast.makeText(this, "Failed to place order. Server returned: " + response,
                                Toast.LENGTH_LONG).show();
                        if (placeOrderButton != null) {
                            placeOrderButton.setEnabled(true);
                        }
                    }
                });
            } catch (IOException e) {
                handler.post(() -> {
                    if (placeOrderButton != null) placeOrderButton.setEnabled(true);
                });
            }
        });
    }

    @Override
    public void onQuantityChanged() {
        updateOrderSummary();
    }
}