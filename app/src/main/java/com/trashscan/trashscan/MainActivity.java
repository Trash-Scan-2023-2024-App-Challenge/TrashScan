package com.trashscan.trashscan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = findViewById(R.id.logInButton);
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = username.getText().toString();
                String p = password.getText().toString();
                TextView t = findViewById(R.id.textView1);
                t.setText("USERNAME: "+u + "\nPASSWORD: "+p);
                // For testing, outputs username & pass. to random textview
                // TODO: Do something with this?
            }
        });
    }
}