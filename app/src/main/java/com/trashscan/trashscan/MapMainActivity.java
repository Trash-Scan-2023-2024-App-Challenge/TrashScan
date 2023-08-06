package com.trashscan.trashscan;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapMainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private String[] countries;
    private String[] usStates;
    private List<LatLng> addedMarkerPositions = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView selectedImageView;
    private String autoFilledCity;
    private String autoFilledZipCode;
    private String autoFilledAddress;
    private String autoFilledCountry;
    private String autoFilledState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_main);

        countries = getResources().getStringArray(R.array.countries_array);
        usStates = getResources().getStringArray(R.array.us_states_array);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);

        Button addMarkerButton = findViewById(R.id.addMarkerButton);
        addMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMarkerDialog();
            }
        });
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Handle the result here
                        selectedImageUri = result.getData().getData();

                        // Show the selected image in the "Add Location" popup
                        if (selectedImageUri != null) {
                            selectedImageView.setVisibility(View.VISIBLE);
                            try {
                                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 500, true);
                                selectedImageView.setImageBitmap(resizedBitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            selectedImageView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showMarkerPopup(Marker marker) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(marker.getTitle());

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Description: ").append(marker.getSnippet()).append("\n");

        LatLng markerPosition = marker.getPosition();
        String city = getAddressComponent(markerPosition, GeocoderConstants.CITY);
        String zipCode = getAddressComponent(markerPosition, GeocoderConstants.ZIP_CODE);
        String address = getAddressComponent(markerPosition, GeocoderConstants.ADDRESS);
        String state = getAddressComponent(markerPosition, GeocoderConstants.STATE);
        String country = getAddressComponent(markerPosition, GeocoderConstants.COUNTRY);


        if (selectedImageUri != null) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(selectedImageUri);

            // Define the desired width and height for the ImageView
            int desiredWidth = 500; // Change this value as needed
            int desiredHeight = 500; // Change this value as needed

            // Resize the image to the desired width and height
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, desiredWidth, desiredHeight, true);
                imageView.setImageBitmap(resizedBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dialogBuilder.setView(imageView);
        }

        if (city != null) messageBuilder.append("City: ").append(city).append("\n");
        if (zipCode != null) messageBuilder.append("Zip Code: ").append(zipCode).append("\n");
        if (address != null) messageBuilder.append("Address: ").append(address).append("\n");
        if (state != null) messageBuilder.append("State: ").append(state).append("\n");
        if (country != null) messageBuilder.append("Country: ").append(country).append("\n");

        dialogBuilder.setMessage(messageBuilder.toString());

        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Add "Delete Pinpoint" option
        dialogBuilder.setNeutralButton("Delete Pinpoint", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove(); // Remove the marker from the map
                addedMarkerPositions.remove(marker.getPosition()); // Remove the position from the list
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    private String getAddressComponent(LatLng latLng, int addressType) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                switch (addressType) {
                    case GeocoderConstants.CITY:
                        return address.getLocality();
                    case GeocoderConstants.ZIP_CODE:
                        return address.getPostalCode();
                    case GeocoderConstants.ADDRESS:
                        return address.getAddressLine(0);
                    case GeocoderConstants.STATE:
                        return address.getAdminArea();
                    case GeocoderConstants.COUNTRY:
                        return address.getCountryName();
                    default:
                        return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class GeocoderConstants {
        public static final int CITY = 0;
        public static final int ZIP_CODE = 1;
        public static final int ADDRESS = 2;
        public static final int STATE = 3;
        public static final int COUNTRY = 4;
    }

    private void showAddMarkerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.map_add_marker_dialog, null);
        dialogBuilder.setView(dialogView);
        selectedImageView = dialogView.findViewById(R.id.selectedImageView);
        EditText cityInput = dialogView.findViewById(R.id.cityInput);
        EditText zipCodeInput = dialogView.findViewById(R.id.zipCodeInput);
        EditText addressInput = dialogView.findViewById(R.id.addressInput);
        EditText parkNameInput = dialogView.findViewById(R.id.parkNameInput);
        EditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        Spinner countrySpinner = dialogView.findViewById(R.id.countrySpinner);
        Spinner stateSpinner = dialogView.findViewById(R.id.stateSpinner);
        ImageView selectedImageView = dialogView.findViewById(R.id.selectedImageView);
        Button selectImageButton = dialogView.findViewById(R.id.selectImageButton);

        String[] countries = getResources().getStringArray(R.array.countries_array);
        String[] usStates = getResources().getStringArray(R.array.us_states_array);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        int unitedStatesIndex = countryAdapter.getPosition("United States");
        countrySpinner.setSelection(unitedStatesIndex);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePickerForLocation();
            }
        });
        List<String> stateList = new ArrayList<>();
        stateList.add("None");
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stateList);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateAdapter);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePickerForLocation();
            }
        });
        if (selectedImageUri != null) {
            selectedImageView.setVisibility(View.VISIBLE);
        } else {
            selectedImageView.setVisibility(View.GONE);
        }

        stateSpinner.setEnabled(false);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        Button addCurrentLocationButton = dialogView.findViewById(R.id.addCurrentLocationButton);
        addCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MapMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapMainActivity.this);
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        double latitude = location.getLatitude();
                                        double longitude = location.getLongitude();

                                        // Use Geocoder to get the address components
                                        Geocoder geocoder = new Geocoder(MapMainActivity.this, Locale.getDefault());
                                        try {
                                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                                            if (addressList != null && !addressList.isEmpty()) {
                                                Address address = addressList.get(0);

                                                // Update the autofill data
                                                autoFilledCity = address.getLocality();
                                                autoFilledZipCode = address.getPostalCode();
                                                autoFilledAddress = address.getAddressLine(0);

                                                // Populate the fields with the autofill data
                                                cityInput.setText(autoFilledCity);
                                                zipCodeInput.setText(autoFilledZipCode);
                                                addressInput.setText(autoFilledAddress);

                                                // Clear the autofill data after populating the fields
                                                autoFilledCity = null;
                                                autoFilledZipCode = null;
                                                autoFilledAddress = null;
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        // Show a toast indicating successful autofill (optional)
                                        Toast.makeText(MapMainActivity.this, "Location autofilled!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MapMainActivity.this, "Unable to get current location. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MapMainActivity.this, "Failed to get current location.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    ActivityCompat.requestPermissions(MapMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }
        });

        if (markerAlreadyExists(null)) {
            addCurrentLocationButton.setEnabled(false);

            Marker existingMarker = getExistingMarker();
            if (existingMarker != null) {
                LatLng markerPosition = existingMarker.getPosition();
                String city = getAddressComponent(markerPosition, GeocoderConstants.CITY);
                String zipCode = getAddressComponent(markerPosition, GeocoderConstants.ZIP_CODE);
                String address = getAddressComponent(markerPosition, GeocoderConstants.ADDRESS);
                String state = getAddressComponent(markerPosition, GeocoderConstants.STATE);
                String country = getAddressComponent(markerPosition, GeocoderConstants.COUNTRY);

                cityInput.setText(city);
                zipCodeInput.setText(zipCode);
                addressInput.setText(address);
                parkNameInput.setText(existingMarker.getTitle());
                descriptionInput.setText(existingMarker.getSnippet());

                if (country.equals("United States")) {
                    stateSpinner.setEnabled(true);
                    ArrayAdapter<String> usStatesAdapter = new ArrayAdapter<>(MapMainActivity.this, android.R.layout.simple_spinner_item, usStates);
                    usStatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    stateSpinner.setAdapter(usStatesAdapter);
                    int stateIndex = usStatesAdapter.getPosition(state);
                    stateSpinner.setSelection(stateIndex);
                }
            }
        } else {
            addCurrentLocationButton.setEnabled(true);
        }

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = parent.getItemAtPosition(position).toString();
                if (selectedCountry.equals("United States")) {
                    stateSpinner.setEnabled(true);
                    ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(MapMainActivity.this, android.R.layout.simple_spinner_item, usStates);
                    stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    stateSpinner.setAdapter(stateAdapter);
                } else {
                    stateSpinner.setEnabled(false);
                    stateList.clear();
                    stateList.add("None");
                    stateAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String city = cityInput.getText().toString().trim();
                String zipCode = zipCodeInput.getText().toString().trim();
                String address = addressInput.getText().toString().trim();
                String parkName = parkNameInput.getText().toString().trim();
                String parkDescription = descriptionInput.getText().toString().trim();
                String country = countrySpinner.getSelectedItem().toString().trim();
                String state = stateSpinner.getSelectedItem().toString().trim();
                if (country.equals("United States") && state.equals("None")) {
                    Toast.makeText(MapMainActivity.this, "Please select a state for the United States", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (parkName.isEmpty() || parkDescription.isEmpty() || city.isEmpty() || zipCode.isEmpty() || address.isEmpty() || selectedImageUri == null) {
                    Toast.makeText(MapMainActivity.this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedImageUri != null) {
                    selectedImageView.setVisibility(View.VISIBLE);
                    selectedImageView.setImageURI(selectedImageUri);
                } else {
                    selectedImageView.setVisibility(View.GONE);
                }

                if (!(parkName.isEmpty() || parkDescription.isEmpty())) {
                    addPinToMap(city, zipCode, address, parkName, parkDescription, country, state);
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityInput.getText().toString().trim();
                String zipCode = zipCodeInput.getText().toString().trim();
                String address = addressInput.getText().toString().trim();
                String parkName = parkNameInput.getText().toString().trim();
                String parkDescription = descriptionInput.getText().toString().trim();
                String country = countrySpinner.getSelectedItem().toString().trim();
                String state = stateSpinner.getSelectedItem().toString().trim();

                // Check for empty fields
                if (parkName.isEmpty()) {
                    Toast.makeText(MapMainActivity.this, "Please enter a park name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (parkDescription.isEmpty()) {
                    Toast.makeText(MapMainActivity.this, "Please enter a park description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (city.isEmpty()) {
                    Toast.makeText(MapMainActivity.this, "Please enter a city", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (zipCode.isEmpty()) {
                    Toast.makeText(MapMainActivity.this, "Please enter a zip code", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (address.isEmpty()) {
                    Toast.makeText(MapMainActivity.this, "Please enter an address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedImageUri == null) {
                    Toast.makeText(MapMainActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the selected country is "United States" and the state is "None"
                if (country.equals("United States") && state.equals("None")) {
                    Toast.makeText(MapMainActivity.this, "Please select a state for the United States", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate other conditions if needed

                // If all required fields are filled, proceed to add the pin to the map
                addPinToMap(city, zipCode, address, parkName, parkDescription, country, state);
                alertDialog.dismiss(); // Dismiss the dialog after successful validation
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Add a new method to handle the image picker for location image selection
    private void openImagePickerForLocation() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Method to handle the result of the image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Show the selected image in the "Add Location" popup
            ImageView locationImageView = findViewById(R.id.selectedImageView);
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 500, 500, true);
                locationImageView.setImageBitmap(resizedBitmap);
                locationImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to upload the selected image
    private void uploadImage() {
        // Implement the image upload logic here.
        // This could involve uploading the image to a server or storing it locally.
        // For this example, we'll simply display a toast message indicating the image was uploaded.

        if (selectedImageUri != null) {
            Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage(Uri imageUri) {
        // Implement the image upload logic here.
        // This could involve uploading the image to a server or storing it locally.
        // For this example, we'll simply display a toast message indicating the image was uploaded.

        if (imageUri != null) {
            // Perform the upload action here
            // For example, show a toast message indicating successful upload
            Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private Marker getExistingMarker() {
        if (!addedMarkerPositions.isEmpty()) {
            return googleMap.addMarker(new MarkerOptions().position(addedMarkerPositions.get(0)).title("").snippet(""));
        }
        return null;
    }


    private void addCurrentLocationPin() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Use Geocoder to get the address components
                                Geocoder geocoder = new Geocoder(MapMainActivity.this, Locale.getDefault());
                                try {
                                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                                    if (addressList != null && !addressList.isEmpty()) {
                                        Address address = addressList.get(0);

                                        // Update the autofill data
                                        autoFilledCity = address.getLocality();
                                        autoFilledZipCode = address.getPostalCode();
                                        autoFilledAddress = address.getAddressLine(0);

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // Show a toast indicating successful autofill (optional)
                                Toast.makeText(MapMainActivity.this, "Location autofilled!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MapMainActivity.this, "Unable to get current location. Make sure location services are enabled.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapMainActivity.this, "Failed to get current location.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void addPinToMap(String city, String zipCode, String address, String parkName,
                             String parkDescription, String country, String state) {
        if (parkName.isEmpty()) {
            Toast.makeText(this, "Please enter a park name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (parkDescription.isEmpty()) {
            Toast.makeText(this, "Please enter a park description", Toast.LENGTH_SHORT).show();
            return;
        }

        String completeAddress = address + ", " + city + ", " + state + ", " + zipCode + ", " + country;

        LatLng parkLatLng = getLocationFromAddress(completeAddress);

        if (parkLatLng != null) {
            if (markerAlreadyExists(parkLatLng)) {
                Toast.makeText(this, "A marker already exists at this location", Toast.LENGTH_SHORT).show();
                return;
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(parkLatLng)
                    .title(parkName)
                    .snippet(parkDescription);

            Marker marker = googleMap.addMarker(markerOptions);

            addedMarkerPositions.add(parkLatLng);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(parkLatLng, 15f));
        } else {
            Toast.makeText(this, "Unable to find the park location", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean markerAlreadyExists(LatLng position) {
        for (LatLng markerPosition : addedMarkerPositions) {
            if (markerPosition.equals(position)) {
                return true;
            }
        }
        return false;
    }

    private LatLng getLocationFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                double latitude = addressList.get(0).getLatitude();
                double longitude = addressList.get(0).getLongitude();
                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addCurrentLocationPin();
            } else {
                Toast.makeText(this, "Location permission denied. You can still add a custom location pin.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                showMarkerPopup(marker);
                return true;
            }
        });
    }
}