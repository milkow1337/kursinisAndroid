package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.CREATE_BASIC_USER_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.BasicUser;
import com.example.prif233.model.VehicleType;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {

    private CheckBox regIsDriver;
    private LinearLayout driverFieldsContainer;
    private Spinner regVehicleTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        regIsDriver = findViewById(R.id.regIsDriver);
        driverFieldsContainer = findViewById(R.id.driverFieldsContainer);
        regVehicleTypeSpinner = findViewById(R.id.regVehicleTypeSpinner);

        regVehicleTypeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, VehicleType.values()));

        regIsDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                driverFieldsContainer.setVisibility(View.VISIBLE);
            } else {
                driverFieldsContainer.setVisibility(View.GONE);
            }
        });
    }

    public void createAccount(View view) {
        EditText login = findViewById(R.id.regLoginField);
        EditText psw = findViewById(R.id.regPasswordField);
        EditText name = findViewById(R.id.regNameField);
        EditText surname = findViewById(R.id.regSurnameField);
        EditText phone = findViewById(R.id.regPhoneField);

        String userInfo = "{}";
        Gson gson = new Gson();

        if (regIsDriver.isChecked()) {
            // Logic for creating a Driver can be added here
            // For now, it just demonstrates the UI change
        } else {
            BasicUser basicUser = new BasicUser(login.getText().toString(), psw.getText().toString(), name.getText().toString(), surname.getText().toString(), phone.getText().toString(), "addressHardcode");
            userInfo = gson.toJson(basicUser, BasicUser.class);
        }

        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String finalUserInfo = userInfo;
        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(CREATE_BASIC_USER_URL, finalUserInfo);
                handler.post(() -> {
                    if (response != null && !response.equals("Error") && !response.isEmpty()) {
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}