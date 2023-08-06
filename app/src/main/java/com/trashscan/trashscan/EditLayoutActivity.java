package com.trashscan.trashscan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class EditLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        Button backButton = (Button) findViewById(R.id.backButton);
        Button saveButton = findViewById(R.id.saveButton);
        EditText emailEdit = findViewById(R.id.email_edit);
        EditText oldPasswordEdit = findViewById(R.id.oldpassword_edit);
        EditText newPasswordEdit = findViewById(R.id.newpassword_edit);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        backButton.setOnClickListener(view -> backActivity());
        // TODO: Make functionality for editing user information.
        // Use mAuth.getCurrentUser().updateEmail(email);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!emailEdit.getText().toString().isEmpty()) {
                        mAuth.getCurrentUser().updateEmail(emailEdit.getText().toString());
                        Log.d("DEBUG:", emailEdit.getText().toString());
                    }


                    if (!oldPasswordEdit.getText().toString().isEmpty() &&
                            !newPasswordEdit.getText().toString().isEmpty()) {
                        mAuth.getCurrentUser().updatePassword(newPasswordEdit.getText().toString());
                    }
                    Intent i = new Intent(getApplicationContext(),ProfileActivity.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    Log.e("Error:",e.toString());
                    Toast.makeText(EditLayoutActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void backActivity() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}