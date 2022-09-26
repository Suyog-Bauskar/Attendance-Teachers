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
    private String emailStr, passwordStr;
    private LinearLayout.LayoutParams params;
    private AlertDialog.Builder alert;
    private LinearLayout layout;
    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        }

        emailField = findViewById(R.id.loginEmailField);
        passwordField = findViewById(R.id.loginPasswordField);
        forgotPassword = findViewById(R.id.loginForgotButton);
        loginBtn = findViewById(R.id.loginLoginButton);

        forgotPassword.setOnClickListener(this);
        loginBtn.setOnClickListener(this);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (10 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginForgotButton:

                alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Reset Password");
                alert.setMessage("Enter Your Email To Received Reset Link.");

                layout = new LinearLayout(getApplicationContext());
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
                break;

            case R.id.loginLoginButton:
                emailStr = emailField.getText().toString().trim();
                passwordStr = passwordField.getText().toString().trim();

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
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
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

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}