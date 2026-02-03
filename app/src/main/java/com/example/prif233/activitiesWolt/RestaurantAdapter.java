package com.example.prif233.activitiesWolt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.prif233.R;
import com.example.prif233.model.Restaurant;

import java.util.List;

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    public RestaurantAdapter(@NonNull Context context, @NonNull List<Restaurant> restaurants) {
        super(context, 0, restaurants);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_restaurant, parent, false);
        }

        Restaurant restaurant = getItem(position);

        if (restaurant != null) {
            TextView nameTextView = convertView.findViewById(R.id.restaurantName);
            TextView addressTextView = convertView.findViewById(R.id.restaurantAddress);
            TextView phoneTextView = convertView.findViewById(R.id.restaurantPhone);
            TextView hoursTextView = convertView.findViewById(R.id.restaurantHours);

            // Display only restaurantName (not name + surname)
            String restaurantName = restaurant.getRestaurantName();
            if (restaurantName != null && !restaurantName.isEmpty()) {
                nameTextView.setText(restaurantName);
            } else {
                // Fallback if restaurantName is not set
                nameTextView.setText(restaurant.getName() != null ? restaurant.getName() : "Restaurant");
            }

            // Display address
            if (restaurant.getAddress() != null && !restaurant.getAddress().isEmpty()) {
                addressTextView.setText("📍 " + restaurant.getAddress());
            } else {
                addressTextView.setText("📍 Address not available");
            }

            // Display phone number
            if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
                phoneTextView.setText("📞 " + restaurant.getPhoneNumber());
            } else {
                phoneTextView.setText("📞 Phone not available");
            }

            // Display opening and closing times
            if (restaurant.getOpeningTime() != null && restaurant.getClosingTime() != null) {
                hoursTextView.setText("🕒 " + restaurant.getOpeningTime() + " - " + restaurant.getClosingTime());
                hoursTextView.setVisibility(View.VISIBLE);
            } else {
                hoursTextView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}