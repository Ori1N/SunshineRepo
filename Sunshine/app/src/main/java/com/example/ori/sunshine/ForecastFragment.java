package com.example.ori.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        // get the selected location from the shared-preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }


    // fetch the weather (using rest) in background thread
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            //android.os.Debug.waitForDebugger(); // only for debugging!!
            Log.d(LOG_TAG, "Starting fetch weather for the location: " + params[0]);

            String[] finalArray = null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.

            // get uri parameters
            String location = params[0],
                    mode = "json",
                    units = "metric"; // always metric. if we want imperial we convert locally
            Integer timePeriod = 7;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

                // declare query parameters
                final String baseUri = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                // declare uri parameters names
                final String PARAM_LOCATION = "q",
                        PARAM_FORMAT = "mode",
                        PARAM_UNITS = "units",
                        PARAM_TIME = "cnt";

                /*
                // Build the Retrofit REST adaptor pointing to the URL specified
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint(baseUri)
                        .build();
                restAdapter.

                        .build();

                // Create an instance of our SimpleApi interface.
                SimpleApi simpleApi = restAdapter.create(SimpleApi.class);

                // Call our method
                System.out.println("simpleApi.simpleGet()=<<" + simpleApi.simpleGet() + ">>");
                //  /
                /*/
                // create basic uri
                Uri uri = Uri.parse(baseUri).buildUpon()
                        // add query parameters
                        .appendQueryParameter(PARAM_LOCATION, location)
                        .appendQueryParameter(PARAM_FORMAT, mode)
                        .appendQueryParameter(PARAM_UNITS, units)
                        .appendQueryParameter(PARAM_TIME, timePeriod.toString())
                                // build uri
                        .build();

                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                Log.d(LOG_TAG, "Connecting to server..");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                Log.d(LOG_TAG, "Reading data..");
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                // the received forecast (Json string)
                forecastJsonStr = buffer.toString();

                // parse the Json data
                try {
                    Log.d(LOG_TAG, "Parsing data..");
                    WeatherParser parser = new WeatherParser();
                    finalArray = parser.getWeatherDataFromJson(forecastJsonStr, timePeriod);
                    Log.d(LOG_TAG, "Done getting weather.");
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error parsing Json", e);
                }

                //  */
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return finalArray;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(String[] strings) {
            Log.d(LOG_TAG, "FetchWeatherTask is over.");
            // if the request succeeded
            if (strings != null) {
                try {
                    Log.d(LOG_TAG, "Updating UI..");
                    adapter.clear();
                    adapter.addAll(strings);
                    adapter.notifyDataSetChanged();
                    Log.d(LOG_TAG, "Done updating UI.");
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to connect adapter", e);
                }
            }

            super.onPostExecute(strings);
        }

        public class WeatherParser {

            private final String LOG_TAG = WeatherParser.class.getSimpleName();

            /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
            private String getReadableDateString(long time){
                // Because the API returns a unix timestamp (measured in seconds),
                // it must be converted to milliseconds in order to be converted to valid date.
                Date date = new Date(time * 1000);
                SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
                return format.format(date).toString();
            }

            /**
             * Prepare the weather high/lows for presentation.
             */
            private String formatHighLows(double high, double low) {

                Log.d(LOG_TAG, "Received temperature range, formatting..");
                // get the current units type
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String unitType = sharedPrefs.getString(
                        getString(R.string.pref_units_key),
                        getString(R.string.pref_units_metric));

                if (unitType.equals(getString(R.string.pref_units_imperial)))
                {
                    high = celsiusToFahrenheit(high);
                    low = celsiusToFahrenheit(low);
                }
                else if (!unitType.equals(getString(R.string.pref_units_metric)))
                    Log.w(LOG_TAG, "Received undefined unit type.");

                // For presentation, assume the user doesn't care about tenths of a degree.
                long roundedHigh = Math.round(high);
                long roundedLow = Math.round(low);

                String highLowStr = roundedHigh + "/" + roundedLow;
                return highLowStr;
            }

            // convert celsius to fahrenheit
            double celsiusToFahrenheit(double cTemp) {
                return (cTemp * 1.8) + 32;
            }

            /**
             * Take the String representing the complete forecast in JSON Format and
             * pull out the data we need to construct the Strings needed for the wireframes.
             *
             * Fortunately parsing is easy:  constructor takes the JSON string and converts it
             * into an Object hierarchy for us.
             */
            public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String OWM_LIST = "list";
                final String OWM_WEATHER = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWM_DATETIME = "dt";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                String[] resultStrs = new String[numDays];
                for(int i = 0; i < weatherArray.length(); i++) {
                    // For now, using the format "Day, description, hi/low"
                    String day;
                    String description;
                    String highAndLow;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = weatherArray.getJSONObject(i);

                    // The date/time is returned as a long.  We need to convert that
                    // into something human-readable, since most people won't read "1400356800" as
                    // "this saturday".
                    long dateTime = dayForecast.getLong(OWM_DATETIME);
                    day = getReadableDateString(dateTime);

                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    // Temperatures are in a child object called "temp".  Try not to name variables
                    // "temp" when working with temperature.  It confuses everybody.
                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high = temperatureObject.getDouble(OWM_MAX);
                    double low = temperatureObject.getDouble(OWM_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;
                }

                // log forecasts
                for (String forecast : resultStrs) {
                    Log.v(LOG_TAG, "Frecast entry " + forecast);
                }

                return resultStrs;
            }

        }

    }
}
