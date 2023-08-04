package com.trashscan.trashscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprof);

        Button button = (Button) findViewById(R.id.password_change);
        Button signOut = (Button) findViewById(R.id.logout);
        TextView email = findViewById(R.id.username);
        email.setText("\nUsername: "+FirebaseAuth.getInstance().getCurrentUser().getEmail()+"\n");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditActivity();
            }
        });
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }
    public void openEditActivity(){
        Intent intent = new Intent(this, EditLayoutActivity.class);
        startActivity(intent);
    }
}