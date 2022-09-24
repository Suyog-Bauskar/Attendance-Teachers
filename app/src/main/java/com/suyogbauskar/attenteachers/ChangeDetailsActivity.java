package com.suyogbauskar.attenteachers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private Button firstnameBtn, lastnameBtn;
    LinearLayout.LayoutParams params;
    private FirebaseFirestore db;
    boolean isProcessRemaining;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_details);

        Toolbar toolbar = findViewById(R.id.toolbarChangeDetails);
        toolbar.setTitle("Change Details");
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        firstnameBtn = findViewById(R.id.firstnameBtn);
        lastnameBtn = findViewById(R.id.lastnameBtn);

        firstnameBtn.setOnClickListener(this);
        lastnameBtn.setOnClickListener(this);

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), (int) (15 * getResources().getDisplayMetrics().density + 0.5f), 0);
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder alert;
        LinearLayout layout;
        isProcessRemaining = false;

        switch (v.getId()) {
            case R.id.firstnameBtn:

                alert = new AlertDialog.Builder(ChangeDetailsActivity.this);
                alert.setTitle("Change Firstname");

                layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText newFirstname = new EditText(getApplicationContext());
                newFirstname.setHint("New firstname");
                newFirstname.setLayoutParams(params);
                layout.addView(newFirstname);

                alert.setView(layout);

                alert.setPositiveButton("Save", (dialog, whichButton) -> {
                    String firstname = newFirstname.getText().toString().trim();

                    if (!firstname.isEmpty()) {
                        db.collection("teachers_data").document(user.getUid()).update("firstname", firstname);
                        Toast.makeText(getApplicationContext(), "Firstname Saved", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid firstname", Toast.LENGTH_LONG).show();
                        isProcessRemaining = true;
                    }
                    if (isProcessRemaining) {
                        isProcessRemaining = false;
                        firstnameBtn.performClick();
                    }
                });

                alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

                alert.show();

                break;

            case R.id.lastnameBtn:

                alert = new AlertDialog.Builder(ChangeDetailsActivity.this);
                alert.setTitle("Change Lastname");

                layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText newLastname = new EditText(getApplicationContext());
                newLastname.setHint("New lastname");
                newLastname.setLayoutParams(params);
                layout.addView(newLastname);

                alert.setView(layout);

                alert.setPositiveButton("Save", (dialog, whichButton) -> {
                    String lastname = newLastname.getText().toString().trim();
                    if (!lastname.isEmpty()) {
                        db.collection("teachers_data").document(user.getUid()).update("lastname", lastname);
                        Toast.makeText(getApplicationContext(), "Lastname Saved", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Invalid lastname", Toast.LENGTH_LONG).show();
                        isProcessRemaining = true;
                    }
                    if (isProcessRemaining) {
                        isProcessRemaining = false;
                        lastnameBtn.performClick();
                    }
                });

                alert.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

                alert.show();

                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }
}