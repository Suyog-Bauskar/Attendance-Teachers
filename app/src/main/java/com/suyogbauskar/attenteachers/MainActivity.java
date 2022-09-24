package com.suyogbauskar.attenteachers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private TextView forgotPassword;
    private Button loginBtn;
    private String emailStr, passwordStr;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginForgotButton:
                final EditText resetMail = new EditText(v.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
                    String mail = resetMail.getText().toString().trim();
                    mAuth.sendPasswordResetEmail(mail)
                            .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Error ! Reset Link is Not Sent", Toast.LENGTH_SHORT).show());

                });

                passwordResetDialog.setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                });

                passwordResetDialog.create().show();
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

                mAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}