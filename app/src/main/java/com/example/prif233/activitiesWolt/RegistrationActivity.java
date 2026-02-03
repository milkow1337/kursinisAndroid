package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.CREATE_BASIC_USER_URL;
import static com.example.prif233.Utils.Constants.CREATE_DRIVER_URL;

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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.LocalDateAdapter;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.BasicUser;
import com.example.prif233.model.Driver;
import com.example.prif233.model.VehicleType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.time.LocalDate;
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
        // Get common fields
        EditText login = findViewById(R.id.regLoginField);
        EditText psw = findViewById(R.id.regPasswordField);
        EditText name = findViewById(R.id.regNameField);
        EditText surname = findViewById(R.id.regSurnameField);
        EditText phone = findViewById(R.id.regPhoneField);
        EditText address = findViewById(R.id.regAddressField);

        // Validate common fields
        String loginText = login.getText().toString().trim();
        String pswText = psw.getText().toString().trim();
        String nameText = name.getText().toString().trim();
        String surnameText = surname.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();
        String addressText = address.getText().toString().trim();

        if (loginText.isEmpty()) {
            Toast.makeText(this, "Please enter login", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pswText.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nameText.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (surnameText.isEmpty()) {
            Toast.makeText(this, "Please enter surname", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phoneText.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if (addressText.isEmpty()) {
            Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
            return;
        }

        String userInfo;
        String url;

        // FIXED: Create Gson with LocalDate adapter for proper serialization
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        if (regIsDriver.isChecked()) {
            // Driver registration
            EditText licence = findViewById(R.id.regLicenseField);
            EditText bDate = findViewById(R.id.regBirthDateField);
            VehicleType vehicleType = (VehicleType) regVehicleTypeSpinner.getSelectedItem();

            String licenceText = licence.getText().toString().trim();
            String bDateText = bDate.getText().toString().trim();

            // Validate driver-specific fields
            if (licenceText.isEmpty()) {
                Toast.makeText(this, "Please enter license number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bDateText.isEmpty()) {
                Toast.makeText(this, "Please enter birth date (YYYY-MM-DD)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate date format
            LocalDate birthDate;
            try {
                birthDate = LocalDate.parse(bDateText);

                // Validate age (must be 18+)
                LocalDate minDate = LocalDate.now().minusYears(18);
                if (birthDate.isAfter(minDate)) {
                    Toast.makeText(this, "You must be at least 18 years old to register as a driver", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid date format. Please use YYYY-MM-DD (e.g., 2000-01-15)", Toast.LENGTH_LONG).show();
                return;
            }

            if (vehicleType == null) {
                Toast.makeText(this, "Please select vehicle type", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create Driver object
            Driver driver = new Driver(
                    loginText,
                    pswText,  // Plain text password - backend will hash it
                    nameText,
                    surnameText,
                    phoneText,
                    addressText,
                    licenceText,
                    birthDate,  // LocalDate will be properly serialized now
                    vehicleType
            );

            userInfo = gson.toJson(driver, Driver.class);
            url = CREATE_DRIVER_URL;

            System.out.println("Creating driver with data: " + userInfo);
        } else {
            // BasicUser registration
            BasicUser basicUser = new BasicUser(
                    loginText,
                    pswText,  // Plain text password - backend will hash it
                    nameText,
                    surnameText,
                    phoneText,
                    addressText
            );

            userInfo = gson.toJson(basicUser, BasicUser.class);
            url = CREATE_BASIC_USER_URL;

            System.out.println("Creating basic user with data: " + userInfo);
        }

        // Send request to backend
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String finalUserInfo = userInfo;
        String finalUrl = url;

        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(finalUrl, finalUserInfo);

                handler.post(() -> {
                    System.out.println("Server response: " + response);

                    if (response != null && !response.equals("Error") && !response.isEmpty()) {
                        Toast.makeText(RegistrationActivity.this,
                                "Registration successful! Please login.",
                                Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = "Registration failed. ";
                        if (response != null && response.contains("already exists")) {
                            errorMessage += "Username already exists.";
                        } else if (response != null && response.contains("18 years old")) {
                            errorMessage += "Driver must be at least 18 years old.";
                        } else {
                            errorMessage += "Please check your information and try again.";
                        }

                        Toast.makeText(RegistrationActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(RegistrationActivity.this,
                            "Network error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}