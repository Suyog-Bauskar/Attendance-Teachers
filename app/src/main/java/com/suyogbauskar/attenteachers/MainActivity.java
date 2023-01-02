package com.suyogbauskar.attenteachers;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.suyogbauskar.attenteachers.utils.ProgressDialog;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private TextView forgotPassword;
    private Button loginBtn;

    private final ProgressDialog progressDialog = new ProgressDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setOnClickListeners();
    }

    private void init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }

        findAllViews();
    }

    private void findAllViews() {
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        forgotPassword = findViewById(R.id.forgotBtn);
        loginBtn = findViewById(R.id.loginBtn);
    }

    private void setOnClickListeners() {
        forgotPassword.setOnClickListener(this::forgotButton);
        loginBtn.setOnClickListener(this::loginButton);
    }

    private void forgotButton(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (10 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0);

        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Reset Password");
        alert.setMessage("Enter Your Email To Received Reset Link.");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText email = new EditText(getApplicationContext());
        email.setHint("Email");
        email.setLayoutParams(params);
        layout.addView(email);

        alert.setView(layout);

        alert.setPositiveButton("Yes", (dialog, whichButton) -> {
            String resetEmail = email.getText().toString().trim();

            if (!resetEmail.isEmpty()) {
                mAuth.sendPasswordResetEmail(resetEmail)
                        .addOnSuccessListener(unused -> Toast.makeText(MainActivity.this, "Reset link sent to " + resetEmail, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_LONG).show();
            }
        });

        alert.setNegativeButton("No", (dialog, whichButton) -> dialog.dismiss());
        alert.show();
    }

    private void loginButton(View view) {
        String emailStr = emailField.getText().toString().trim();
        String passwordStr = passwordField.getText().toString().trim();

        if (emailStr.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordStr.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Invalid Password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordStr.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password must be greater than 5 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show(MainActivity.this);

        mAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(task -> {
                    progressDialog.hide();
                    if (task.isSuccessful()) {
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}