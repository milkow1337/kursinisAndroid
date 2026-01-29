package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.ASSIGN_DRIVER;
import static com.example.prif233.Utils.Constants.GET_ALL_RESTAURANTS_URL;
import static com.example.prif233.Utils.Constants.GET_AVAILABLE_ORDERS;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
import com.example.prif233.model.Driver;
import com.example.prif233.model.FoodOrder;
import com.example.prif233.model.Restaurant;
import com.example.prif233.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WoltRestaurants extends AppCompatActivity {

    User currentUser;
    String userInfoJson;
    boolean isDriver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wolt_restaurants);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        userInfoJson = intent.getStringExtra("userJsonObject");

        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        build.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        Gson gson = build.create();
        currentUser = gson.fromJson(userInfoJson, User.class);

        isDriver = currentUser instanceof Driver || (userInfoJson != null && userInfoJson.contains("licence"));

        if (isDriver) {
            loadAvailableOrders();
        } else if (currentUser instanceof Restaurant) {
            // Not supported in this app
        } else {
            loadRestaurants();
        }
    }

    private void loadRestaurants() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_ALL_RESTAURANTS_URL);
                handler.post(() -> {
                    try {
                        if (response != null && !response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
                            Gson gsonRestaurants = gsonBuilder.create();
                            Type restaurantListType = new TypeToken<List<Restaurant>>() {}.getType();
                            List<Restaurant> restaurantListFromJson = gsonRestaurants.fromJson(response, restaurantListType);

                            ListView restaurantListElement = findViewById(R.id.restaurantList);
                            RestaurantAdapter adapter = new RestaurantAdapter(this, restaurantListFromJson);
                            restaurantListElement.setAdapter(adapter);

                            restaurantListElement.setOnItemClickListener((parent, view, position, id) -> {
                                Restaurant selectedRestaurant = restaurantListFromJson.get(position);
                                Intent intentMenu = new Intent(WoltRestaurants.this, MenuActivity.class);
                                intentMenu.putExtra("restaurantId", selectedRestaurant.getId());
                                intentMenu.putExtra("userId", currentUser.getId());
                                startActivity(intentMenu);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading restaurants: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadAvailableOrders() {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_AVAILABLE_ORDERS);
                handler.post(() -> {
                    try {
                        if (response != null && !response.equals("Error")) {
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
                            gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
                            Gson gsonOrders = gsonBuilder.create();
                            Type orderListType = new TypeToken<List<FoodOrder>>() {}.getType();
                            List<FoodOrder> availableOrders = gsonOrders.fromJson(response, orderListType);

                            ListView orderListElement = findViewById(R.id.restaurantList);
                            MyOrdersAdapter adapter = new MyOrdersAdapter(this, availableOrders);
                            orderListElement.setAdapter(adapter);

                            orderListElement.setOnItemClickListener((parent, view, position, id) -> {
                                FoodOrder selectedOrder = availableOrders.get(position);
                                pickUpOrder(selectedOrder.getId());
                            });
                            
                            setTitle("Available Orders for Delivery");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void pickUpOrder(int orderId) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        JsonObject json = new JsonObject();
        json.addProperty("driverId", currentUser.getId());

        executor.execute(() -> {
            try {
                String url = ASSIGN_DRIVER + orderId + "/assignDriver";
                String response = RestOperations.sendPost(url, json.toString());
                handler.post(() -> {
                    if (response != null && !response.equals("Error")) {
                        Toast.makeText(this, "Order assigned to you!", Toast.LENGTH_SHORT).show();
                        loadAvailableOrders(); 
                    } else {
                        Toast.makeText(this, "Failed to pick up order", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void viewPurchaseHistory(View view) {
        Intent intent = new Intent(WoltRestaurants.this, MyOrders.class);
        intent.putExtra("id", currentUser.getId());
        intent.putExtra("isDriver", isDriver);
        startActivity(intent);
    }

    public void viewMyAccount(View view) {
        Intent intent = new Intent(WoltRestaurants.this, UserInfoActivity.class);
        intent.putExtra("userJsonObject", userInfoJson);
        startActivity(intent);
    }
}