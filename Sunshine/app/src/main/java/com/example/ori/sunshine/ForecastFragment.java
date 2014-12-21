package com.example.ori.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> adapter;
    private View rootView;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // for fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    /**/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // inflate menu item
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            // if the refresh-option was selected
            case R.id.action_refresh:
            {
                Log.d(LOG_TAG, "The refresh option was selected from ForecastFragment.");
                updateWeather();
                return true;
            }
            // if the settings-option was selected
            case R.id.action_settings:
            {
                Log.d(LOG_TAG, "The settings option was selected from ForecastFragment.");
                // call different activity
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Creating view..");
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // create the fake data
        String[] forecastsArray = {
                "Today - Sunny",
                "Tomorrow - Foggy"
        };
        List<String> forecasts = new ArrayList<String>(Arrays.asList(forecastsArray));

        // create the adapter
        adapter = new ArrayAdapter<String>(
                // the current context
                getActivity(),
                // the layout of the list
                R.layout.list_item_forecast,
                // the layout of each list item
                R.id.list_item_forecast_textview,
                // the data array
                forecasts);

        // get the actual list
        ListView actualListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        // connect the list to the adapter
        actualListView.setAdapter(adapter);

        // set the function that happens when a list item is clicked
        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "A weather item was clicked.");

                // get the selected forecast
                String forecast = adapter.getItem(position);

                // call different activity
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });
        //
        return rootView;
    }


    private void updateWeather() {
        // call update weather
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), adapter);
        // get the selected location from the shared-preferences
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

}
