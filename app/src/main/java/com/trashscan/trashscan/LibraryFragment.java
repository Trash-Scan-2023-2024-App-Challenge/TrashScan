package com.trashscan.trashscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;


public class LibraryFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_library, container, false);

        Button button = (Button) v.findViewById(R.id.password_change);
        Button signOut = (Button) v.findViewById(R.id.logout);
        TextView email = v.findViewById(R.id.username);
        email.setText("\nUsername: "+ FirebaseAuth.getInstance().getCurrentUser().getEmail()+"\n");
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
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    public void openEditActivity(){
        Intent intent = new Intent(getActivity(), EditLayoutActivity.class);
        startActivity(intent);
    }
}