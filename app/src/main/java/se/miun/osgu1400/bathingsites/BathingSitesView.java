package se.miun.osgu1400.bathingsites;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BathingSitesView extends RelativeLayout {

    private TextView numberOfBathingSites;
    private TextView numberOfBathingSitesText;

    public BathingSitesView(Context context) {
        super(context);
    }

    public BathingSitesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Inflate xml file for the component
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bathing_sites_view, this);

        numberOfBathingSites = (TextView) findViewById(R.id.numberOfBathingSites);
        numberOfBathingSitesText = (TextView) findViewById(R.id.numberOfBathingSitesText);
    }

    // Get number of saved bathing sites
    public int getNumberOfBathingSites() {
        return Integer.valueOf(this.numberOfBathingSites.toString());
    }

    // Set number of bathing sites to view it in the component
    public void setNumberOfBathingSites(int newNumber) {
        if (newNumber == 1) {
            numberOfBathingSitesText.setText(R.string.text_for_one_bathing_site);
        } else {
            numberOfBathingSitesText.setText(R.string.text_for_multiple_bathing_sites);
        }
        numberOfBathingSites.setText(String.valueOf(newNumber));
    }
}
