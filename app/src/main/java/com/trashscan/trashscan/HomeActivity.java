package com.trashscan.trashscan;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.trashscan.trashscan.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment()); // Home screen
            } else if (item.getItemId() == R.id.shorts) {
                replaceFragment(new ShortsFragment()); // Explore screen
            } else if (item.getItemId() == R.id.subscriptions) {
                replaceFragment(new SubscriptionFragment());
            } else if (item.getItemId() == R.id.library) {
                replaceFragment(new LibraryFragment()); // User profile screen
            } else if (item.getItemId() == R.id.fab) {
                replaceFragment(new MapsButtonFragment());
            }

            return true;
        });

        // Add the HomeFragment as the default fragment
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}