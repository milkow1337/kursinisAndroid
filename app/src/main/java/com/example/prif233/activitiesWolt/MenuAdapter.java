package com.example.prif233.activitiesWolt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.prif233.R;
import com.example.prif233.model.Cuisine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuAdapter extends BaseAdapter {
    private Context context;
    private List<Cuisine> menuItems;
    private Map<Integer, Integer> quantities; // Map of cuisine ID to quantity
    private OnQuantityChangeListener quantityChangeListener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public MenuAdapter(Context context, List<Cuisine> menuItems) {
        this.context = context;
        this.menuItems = menuItems;
        this.quantities = new HashMap<>();
        if (context instanceof OnQuantityChangeListener) {
            this.quantityChangeListener = (OnQuantityChangeListener) context;
        }
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return menuItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        }

        Cuisine cuisine = menuItems.get(position);
        int cuisineId = cuisine.getId();

        TextView nameTextView = convertView.findViewById(R.id.menuItemName);
        TextView ingredientsTextView = convertView.findViewById(R.id.menuItemIngredients);
        TextView priceTextView = convertView.findViewById(R.id.menuItemPrice);
        TextView quantityTextView = convertView.findViewById(R.id.itemQuantity);
        TextView spicyTextView = convertView.findViewById(R.id.menuItemSpicy);
        TextView veganTextView = convertView.findViewById(R.id.menuItemVegan);
        Button increaseButton = convertView.findViewById(R.id.btnIncrease);
        Button decreaseButton = convertView.findViewById(R.id.btnDecrease);

        nameTextView.setText(cuisine.getName());
        
        if (cuisine.getIngredients() != null && !cuisine.getIngredients().isEmpty()) {
            ingredientsTextView.setText(cuisine.getIngredients());
        } else {
            ingredientsTextView.setVisibility(View.GONE);
        }

        if (cuisine.getPrice() != null) {
            priceTextView.setText(String.format("€%.2f", cuisine.getPrice()));
        } else {
            priceTextView.setText("€0.00");
        }

        spicyTextView.setVisibility(cuisine.isSpicy() ? View.VISIBLE : View.GONE);
        veganTextView.setVisibility(cuisine.isVegan() ? View.VISIBLE : View.GONE);

        int currentQuantity = quantities.getOrDefault(cuisineId, 0);
        quantityTextView.setText(String.valueOf(currentQuantity));

        increaseButton.setTag(cuisineId);
        decreaseButton.setTag(cuisineId);


        increaseButton.setOnClickListener(v -> {
            Integer id = (Integer) v.getTag();
            int qty = quantities.getOrDefault(id, 0);
            quantities.put(id, qty + 1);

            View parentView = (View) v.getParent();
            TextView qtyView = parentView.findViewById(R.id.itemQuantity);
            if (qtyView != null) {
                qtyView.setText(String.valueOf(qty + 1));
            }
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChanged();
            }
        });


        decreaseButton.setOnClickListener(v -> {
            Integer id = (Integer) v.getTag();
            int qty = quantities.getOrDefault(id, 0);
            if (qty > 0) {
                quantities.put(id, qty - 1);

                View parentView = (View) v.getParent();
                TextView qtyView = parentView.findViewById(R.id.itemQuantity);
                if (qtyView != null) {
                    qtyView.setText(String.valueOf(qty - 1));
                }
                if (quantityChangeListener != null) {
                    quantityChangeListener.onQuantityChanged();
                }
            }
        });

        return convertView;
    }

    public Map<Integer, Integer> getQuantities() {
        return quantities;
    }

    public List<Cuisine> getMenuItems() {
        return menuItems;
    }
}

