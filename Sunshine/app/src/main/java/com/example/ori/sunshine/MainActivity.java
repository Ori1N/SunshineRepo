package com.example.ori.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id)
        {
            // if the settings-option was selected
            case R.id.action_settings:
            {
                Log.d(LOG_TAG, "Settings were selected.");
                // call settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            // if the on-map-option was selected
            case R.id.action_map:
            {
                openPreferredLocationOnMap();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    void openPreferredLocationOnMap() {
        // get the selected location from the shared-preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        // convert it to geographic location
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location).build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        // if the intent can be activated successfully
        if (intent.resolveActivity(getPackageManager()) != null) {
            // run it
            startActivity(intent);
        // else
        } else {
            // log a warning message
            Log.w(LOG_TAG, "Couldn't show the location in a map.");
        }
    }
    //  */

}
