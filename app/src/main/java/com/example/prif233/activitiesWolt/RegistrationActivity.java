package com.example.prif233.activitiesWolt;

import static com.example.prif233.Utils.Constants.CREATE_BASIC_USER_URL;
import static com.example.prif233.Utils.Constants.VALIDATE_USER_URL;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prif233.R;
import com.example.prif233.Utils.RestOperations;
import com.example.prif233.model.BasicUser;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {

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
    }

    public void createAccount(View view) {

        TextView login = findViewById(R.id.regLoginField);
        TextView psw = findViewById(R.id.regPasswordField);
        TextView name = findViewById(R.id.regNameField);
        TextView surname = findViewById(R.id.regSurnameField);
        TextView phone = findViewById(R.id.regPhoneField);
        //susirenkat likusius duomenis

        //Patikrinti, ar buvo pasirinktas driver ar ne
        String userInfo = "{}";
        if (findViewById(R.id.regIsDriver).isActivated()) {
            //Kurt driver
        } else {

            BasicUser basicUser = new BasicUser(login.getText().toString(), psw.getText().toString(), name.getText().toString(), surname.getText().toString(), phone.getText().toString(), "addressHardcode");
            Gson gson = new Gson();
            userInfo = gson.toJson(basicUser, BasicUser.class);
            System.out.println(userInfo);
        }


        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        String finalUserInfo = userInfo;
        executor.execute(() -> {
            try {
                String response = RestOperations.sendPost(CREATE_BASIC_USER_URL, finalUserInfo);
                handler.post(() -> {
                    if (!response.equals("Error") && !response.isEmpty()) {
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            } catch (IOException e) {
                //Toast reikes
            }

        });

    }
}