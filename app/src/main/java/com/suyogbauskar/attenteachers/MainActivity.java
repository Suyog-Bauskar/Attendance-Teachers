package com.suyogbauskar.attenteachers;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private TextView forgotPassword;
    private Button loginBtn;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        findAllViews();
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
    }

    private void findAllViews() {
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        forgotPassword = findViewById(R.id.forgotBtn);
        loginBtn = findViewById(R.id.loginBtn);
    }

    private void setOnClickListeners() {
        forgotPassword.setOnClickListener(this);
        loginBtn.setOnClickListener(this);
    }

    private void showProgressDialog() {
        pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void forgotButton() {
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
                        .addOnSuccessListener(unused -> Toast.makeText(MainActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error ! Reset Link is Not Sent", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_LONG).show();
            }
        });

        alert.setNegativeButton("No", (dialog, whichButton) -> dialog.dismiss());
        alert.show();
    }

    private void loginButton() {
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

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(task -> {
            hideProgressDialog();
            if (task.isSuccessful()) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            } else {
                Toast.makeText(MainActivity.this, "Try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgotBtn:
                forgotButton();
                break;

            case R.id.loginBtn:
                loginButton();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}