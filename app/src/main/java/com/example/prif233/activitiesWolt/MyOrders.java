package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.GET_ORDERS_BY_USER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.FoodOrder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyOrders extends AppCompatActivity {

    private int userId;

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

        //Noriu uzkrauti orderius konkreciam klientui

        Intent intent = getIntent();
        userId = intent.getIntExtra("id", 0);

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String response = RestOperations.sendGet(GET_ORDERS_BY_USER + userId);
                System.out.println(response);
                handler.post(() -> {
                    try {
                        if (!response.equals("Error")) {
                            Type ordersListType = new TypeToken<List<FoodOrder>>() {
                            }.getType();
                            List<FoodOrder> ordersListFromJson = new com.google.gson.Gson().fromJson(response, ordersListType);
                            ListView ordersListElement = findViewById(R.id.myOrderList);
                            MyOrdersAdapter adapter = new MyOrdersAdapter(this, ordersListFromJson);
                            ordersListElement.setAdapter(adapter);

                            ordersListElement.setOnItemClickListener((parent, view, position, id) -> {
                                System.out.println(ordersListFromJson.get(position));
                                Intent intentChat = new Intent(MyOrders.this, ChatSystem.class);
                                intentChat.putExtra("orderId", ordersListFromJson.get(position).getId());
                                intentChat.putExtra("userId", userId);
                                startActivity(intentChat);
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