package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.GET_ALL_RESTAURANTS_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.LocalDateTimeAdapter;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.Driver;
import com.example.prif233.model.Restaurant;
import com.example.prif233.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WoltRestaurants extends AppCompatActivity {

    User currentUser;

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

        //Priejimas prie duomenu is praeitos Activity

        Intent intent = getIntent();
        String userInfo = intent.getStringExtra("userJsonObject");


        GsonBuilder build = new GsonBuilder();
        build.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        Gson gson = build.setPrettyPrinting().create();
        currentUser = gson.fromJson(userInfo, User.class);

        if (currentUser instanceof Driver) {

        } else if (currentUser instanceof Restaurant) {
            //net neleisim sito
        } else {
            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                try {
                    String response = RestOperations.sendGet(GET_ALL_RESTAURANTS_URL);
                    System.out.println(response);
                    handler.post(() -> {
                        try {
                            if (!response.equals("Error")) {
                                //Cia yra dalis, kaip is json, kuriame yra [{},{}, {},...] paversti i List is Restoranu

                                GsonBuilder gsonBuilder = new GsonBuilder();
                                gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
                                Gson gsonRestaurants = gsonBuilder.setPrettyPrinting().create();
                                Type restaurantListType = new TypeToken<List<Restaurant>>() {
                                }.getType();
                                List<Restaurant> restaurantListFromJson = gsonRestaurants.fromJson(response, restaurantListType);
                                //Json parse end

                                //Reikia tuos duomenis, kuriuos ka tik isparsinau is json, atvaizduoti grafiniam elemente
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
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    public void viewPurchaseHistory(View view) {
        Intent intent = new Intent(WoltRestaurants.this, MyOrders.class);
        intent.putExtra("id", currentUser.getId());
        startActivity(intent);
    }

    public void viewMyAccount(View view) {
        //Arba naujas activity arba fragmentas - account redagavimo forma
    }
}