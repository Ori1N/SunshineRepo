package com.example.ori.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.TextView;

import com.example.ori.sunshine.app.data.WeatherContract;
import com.example.ori.sunshine.app.data.WeatherContract.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final String LOG_TAG = ForecastFragment.class.getSimpleName();

    //private ArrayAdapter<String> mForecastAdapter;
    private SimpleCursorAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // for fragment to handle menu events
        setHasOptionsMenu(true);
    }

    /* before we used content-provider and cursor-loader we needed to initialize our weather data onStart
    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }
    //  */
    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
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
        switch (id) {
            // if the refresh-option was selected
            case R.id.action_refresh: {
                Log.d(LOG_TAG, "The refresh option was selected from ForecastFragment.");
                updateWeather();
                return true;
            }
            // if the settings-option was selected
            case R.id.action_settings: {
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
        /*/ fake data
        String[] forecastsArray = {
                "Today - Sunny",
                "Tomorrow - Foggy"
        };
        List<String> forecasts = new ArrayList<String>(Arrays.asList(forecastsArray));
        //  */

        // The SimpleCursorAdapter will take data from the database through the
        // Loader and use it to populate the ListView it's attached to.
        mForecastAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_forecast,
                null,
                // the column names to use to fill the textviews
                new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                },
                // the textviews to fill with the data pulled from the columns above
                new int[]{R.id.list_item_date_textview,
                        R.id.list_item_forecast_textview,
                        R.id.list_item_high_textview,
                        R.id.list_item_low_textview
                },
                0
        );
        // bind the cursorLoader to the SimpleCursorAdapter
        mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                boolean isMetric = Utility.isMetric(getActivity());
                switch (columnIndex) {
                    case COL_WEATHER_MAX_TEMP:
                    case COL_WEATHER_MIN_TEMP: {
                        // we have to do some formatting and possibly a conversion
                        ((TextView) view).setText(Utility.formatTemperature(
                                cursor.getDouble(columnIndex), isMetric));
                        return true;
                    }
                    case COL_WEATHER_DATE: {
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utility.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

        /*/ create the mForecastAdapter
        mForecastAdapter = new ArrayAdapter<String>(
                // the current context
                getActivity(),
                // the layout of the list
                R.layout.list_item_forecast,
                // the layout of each list item
                R.id.list_item_forecast_textview,
                // the data array
                forecasts);
        //  */

        Log.d(LOG_TAG, "Creating view..");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // get the actual list
        ListView actualListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        // connect the list to the mForecastAdapter
        actualListView.setAdapter(mForecastAdapter);

        // set the function that happens when a list item is clicked
        actualListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "A weather item was clicked.");

                        // get the cursor
                        Cursor cursor = mForecastAdapter.getCursor();
                        // if there is a cursor and it can be set to the received position
                        if (cursor != null && cursor.moveToPosition(position)) {
                            // get the data from the cursor (and our activity)
                            String dateString = Utility.formatDate(cursor.getString(COL_WEATHER_DATE));
                            /*
                            String weatherDescription = cursor.getString(COL_WEATHER_DESC);
                            boolean isMetric = Utility.isMetric(getActivity());
                            String high = Utility.formatTemperature(
                                    cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
                            String low = Utility.formatTemperature(
                                    cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

                            // format the data
                            String detailString = String.format("%s - %s - %s/%s",
                                    dateString, weatherDescription, high, low);
                            //  */

                            // format the data
                            String detailString = dateString;

                            // use intent to call detail-activity
                            Intent intent = new Intent(getActivity(), DetailActivity.class)
                                    .putExtra(Intent.EXTRA_TEXT, detailString);
                            startActivity(intent);
                        }
                    }
                });
        //
        return rootView;
    }


    private void updateWeather() {
        // call update weather
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        // get the selected location from the shared-preferences
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    // LOADER -------------------------------------------------------

    private static final int FORECAST_LOADER = 0;
    private String mLocation;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
