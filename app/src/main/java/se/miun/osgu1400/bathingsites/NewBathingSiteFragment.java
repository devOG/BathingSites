package se.miun.osgu1400.bathingsites;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;

public class NewBathingSiteFragment extends Fragment {

    private EditText name;
    private EditText description;
    private EditText address;
    private EditText longitude;
    private EditText latitude;
    private EditText waterTemp;
    private EditText dateForTemp;
    private RatingBar ratingBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_bathing_site, container, false);

        name = (EditText) view.findViewById(R.id.nameInput);
        description = (EditText) view.findViewById(R.id.descriptionInput);
        address = (EditText) view.findViewById(R.id.addressInput);
        longitude = (EditText) view.findViewById(R.id.longitudeInput);
        latitude = (EditText) view.findViewById(R.id.latitudeInput);
        waterTemp = (EditText) view.findViewById(R.id.waterTempInput);
        dateForTemp = (EditText) view.findViewById(R.id.dateForTempInput);
        ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);

        return view;
    }

    // Getters for location
    public EditText getName() {
        return name;
    }

    public EditText getDescription() {
        return description;
    }

    public EditText getAddress() {
        return address;
    }

    public EditText getLongitude() {
        return longitude;
    }

    public EditText getLatitude() {
        return latitude;
    }

    public EditText getWaterTemp() {
        return waterTemp;
    }

    public EditText getDateForTemp() {
        return dateForTemp;
    }

    public RatingBar getRatingBar() {
        return ratingBar;
    }

    // Reset all fields
    public void clearAllFields() {
        name.setText("");
        description.setText("");
        address.setText("");
        longitude.setText("");
        latitude.setText("");
        waterTemp.setText("");
        dateForTemp.setText("");
        ratingBar.setRating(0);

        name.requestFocus();
    }

    // Validate input from user
    public boolean validateInput() {

        boolean validationPassed = true;

        if (name.getText().toString().isEmpty()) {
            name.setError("Name is required");
            validationPassed = false;
        }

        if (address.getText().toString().isEmpty()) {

            // If address fails, check long/lat
            if (longitude.getText().toString().isEmpty() ||
                    latitude.getText().toString().isEmpty()) {

                // Check which failed and show error
                if (longitude.getText().toString().isEmpty()) {
                    longitude.setError("Longitude is required");
                    validationPassed = false;
                }
                if (latitude.getText().toString().isEmpty()) {
                    latitude.setError("Latitude is required");
                    validationPassed = false;
                }

                // Address error is only shown if long/lat also fails, and long/lat error
                // is only shown if error also fails.
                // This way, we don't show error message that is not needed and mislead
                // the user, since only address OR long/lat is needed.
                address.setError("Address is required");
            }
        }

        return validationPassed;
    }
}
