package com.trashscan.trashscan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class ShortsFragment extends Fragment {

    private ImageView parkImageView;
    private TextView parkNameTextView;
    private TextView parkDescriptionTextView;
    // Add other views to display city, zip code, state, country, address here

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shorts, container, false);

        parkImageView = rootView.findViewById(R.id.parkImageView);
        parkNameTextView = rootView.findViewById(R.id.parkNameTextView);
        parkDescriptionTextView = rootView.findViewById(R.id.parkDescriptionTextView);
        // Initialize other views here

        // Retrieve park information from arguments and update the views
        Bundle args = getArguments();
        if (args != null) {
            String parkName = args.getString("parkName");
            String parkDescription = args.getString("parkDescription");
            // Get other information from args

            // Update the views with the received information
            parkNameTextView.setText(parkName);
            parkDescriptionTextView.setText(parkDescription);
            // Update other views with their respective information
        }

        return rootView;
    }
}