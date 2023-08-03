package com.trashscan.trashscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicators;
    private MaterialButton buttonOnboardingAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        layoutOnboardingIndicators = findViewById(R.id.layoutOnboardingIndicators);
        buttonOnboardingAction = findViewById(R.id.buttonOnboardingAction);

        setupOnboardingItems();

        ViewPager2 onboardingViewPager = findViewById(R.id.onboardingViewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);

        setupOnboardingIndicators();
        setCurrentOnboardingIndicator(0);

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicator(position);
            }
        });

        Log.d("DEBUG", "Everything working so far");

        buttonOnboardingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()){
                    onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
                }else{
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                }
            }
        });
    }

    private void setupOnboardingItems(){
        List<OnboardingItem> onboardingItems = new ArrayList<>();
        OnboardingItem itemTrashScanIntro = new OnboardingItem();
        itemTrashScanIntro.setTitle("Welcome to TrashScan");
        itemTrashScanIntro.setDescription("TrashScan is an environmental cleanup app that allows you access to various cleanup sites in your area!");
        itemTrashScanIntro.setImage(R.drawable.trash);

        OnboardingItem itemTakePicture = new OnboardingItem();
        itemTakePicture.setTitle("Picture Perfect");
        itemTakePicture.setDescription("Take a picture of a nearby area that either needs cleanup or has been cleaned up!");
        itemTakePicture.setImage(R.drawable.camera);

        OnboardingItem itemPlaceOnMap = new OnboardingItem();
        itemPlaceOnMap.setTitle("Map It Out");
        itemPlaceOnMap.setDescription("Place your clean up site on our map to alert others in your area!");
        itemPlaceOnMap.setImage(R.drawable.location_icon);

        onboardingItems.add(itemTrashScanIntro);
        onboardingItems.add(itemTakePicture);
        onboardingItems.add(itemPlaceOnMap);

        onboardingAdapter = new OnboardingAdapter(onboardingItems);
    }

    private void setupOnboardingIndicators(){
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8,0,8,0);
        for(int i = 0; i < indicators.length; i++){
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.onboarding_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicators.addView(indicators[i]);
        }
    }

    private void setCurrentOnboardingIndicator(int index){
        int childCount = layoutOnboardingIndicators.getChildCount();
        for(int i = 0; i < childCount; i++){
            ImageView imageView = (ImageView) layoutOnboardingIndicators.getChildAt(i);
            if(i == index){
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.onboarding_indicator_active)
                );
            }else{
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_inactive)
                );
            }
        }
        if(index == onboardingAdapter.getItemCount()-1){
            buttonOnboardingAction.setText("Start");
            Log.d("DEBUG", "On Last Page");
            new Handler().postDelayed(new Runnable() {
                public void run () {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                     startNext(); // Goes to admin or home page
                }
            }, 5000L);

        }else{
            buttonOnboardingAction.setText("Next");
        }
    }

    public void startNext() {
        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(curUser.getUid());
        mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("Debug:taskSuccessful",
                            "Value: "+task.getResult().exists());
                    if (task.getResult().exists()) {
                        Toast.makeText(OnboardingActivity.this, "IS ADMIN", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
                        //finish();
                    } else
                        Toast.makeText(OnboardingActivity.this, "NOT ADMIN", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Debug:taskFailed", "Error getting data");
                }
            }
        });
    }
}