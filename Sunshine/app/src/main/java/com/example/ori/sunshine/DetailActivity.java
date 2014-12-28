package com.example.ori.sunshine;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends ActionBarActivity {

    public static final String DATE_KEY = "forecast_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            String date = getIntent().getStringExtra(DATE_KEY);

            Bundle arguments = new Bundle();
            arguments.putString(DetailActivity.DATE_KEY, date);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}



/**
     * A placeholder fragment containing a simple view.
     *//*

    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String LOG_TAG = PlaceholderFragment.class.getSimpleName();

        private final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
        private String mForecastStr;

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);

            */
/*
            // on create - set text to received text
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            {
                mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                TextView textView = (TextView) rootView.findViewById(R.id.textview);
                textView.setText(mForecastStr);
            }

            return rootView;
            //  *//*

        }


        private static final int DETAIL_LOADER = 0;

        private ShareActionProvider mShareActionProvider;
        private String mLocation;
        private String mForecast;

        private static final String[] FORECAST_COLUMNS = {
                WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
                WeatherEntry.COLUMN_DATETEXT,
                WeatherEntry.COLUMN_SHORT_DESC,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
        };


        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(LOCATION_KEY, mLocation);
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mLocation != null &&
                    !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            if (savedInstanceState != null) {
                mLocation = savedInstanceState.getString(LOCATION_KEY);
            }
            super.onActivityCreated(savedInstanceState);
        }



        // menu item handling functions --------------------------------

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // inflate the menu - add items to the action bar (if present)
            inflater.inflate(R.menu.detailfragment, menu);

            // retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // get the provider and hold onto it to set/change the share intent
            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // attach an intent to this ShareActionProvider, updatable
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.w(LOG_TAG, "Share action provider is not available (null).");
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            // make sure we stay in our app after sharing (and not focusing on the sharing app)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }


        // loader -------------------------------------------------------

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null || !intent.hasExtra(DATE_KEY)) {
                return null;
            }
            String forecastDate = intent.getStringExtra(DATE_KEY);

            // Sort order:  Ascending, by date.
            String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

            mLocation = Utility.getPreferredLocation(getActivity());
            Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithDate(
                    mLocation, forecastDate);
            Log.v(LOG_TAG, weatherForLocationUri.toString());

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
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            String dateString = Utility.formatDate(
                    data.getString(data.getColumnIndex(WeatherEntry.COLUMN_DATETEXT)));
            ((TextView) getView().findViewById(R.id.detail_date_textview))
                    .setText(dateString);

            String weatherDescription =
                    data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            ((TextView) getView().findViewById(R.id.detail_forecast_textview))
                    .setText(weatherDescription);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.detail_high_textview)).setText(high);

            String low = Utility.formatTemperature(
                    data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            ((TextView) getView().findViewById(R.id.detail_low_textview)).setText(low);

            // We still need this for the share intent
            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            Log.v(LOG_TAG, "Forecast String: " + mForecast);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }
}
*/
