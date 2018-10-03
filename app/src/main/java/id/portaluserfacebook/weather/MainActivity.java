package id.portaluserfacebook.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import id.portaluserfacebook.R;
import id.portaluserfacebook.weather.utilities.Forecast;
import id.portaluserfacebook.weather.utilities.ForecastAdapter;
import id.portaluserfacebook.weather.utilities.RemoteFetch;
import id.portaluserfacebook.weather.utilities.SetListViewHeight;

public class MainActivity extends Fragment implements LocationListener {
    private TextView mTemp, mHumidity, mTempHigh, mTempLow, mName, mWeather, mWeatherIcon;
    private ListView mListViewForecast;
    private List<Forecast> arrayListForecast;
    private Handler handler;
    private LocationManager locationManager;
    private long tenMin = 10 * 60 * 1000;
    private long oneMile = 1609; // meters

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        Typeface weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Weather-Fonts.ttf");
        Typeface robotoThin = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        Typeface robotoLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");

        mListViewForecast = (ListView) view.findViewById(R.id.listView);
        mListViewForecast.setEnabled(false);
        mTemp = (TextView) view.findViewById(R.id.temp);
        mHumidity = (TextView) view.findViewById(R.id.humidity);
        mTempHigh = (TextView) view.findViewById(R.id.tempHigh);
        mTempLow = (TextView) view.findViewById(R.id.tempLow);
        mName = (TextView) view.findViewById(R.id.name);
        mWeather = (TextView) view.findViewById(R.id.weather);
        mWeatherIcon = (TextView) view.findViewById(R.id.weatherIcon);

        mWeatherIcon.setTypeface(weatherFont);
        mTemp.setTypeface(robotoThin);
        mName.setTypeface(robotoLight);
        mWeather.setTypeface(robotoLight);

        handler = new Handler();
        arrayListForecast = new ArrayList<>();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this); // remove the location updates
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tenMin, oneMile, this);
    }

    private void updateWeather(final String lat, final String lon) {
        new Thread() {
            public void run() {
                final JSONObject jsonCurrent = RemoteFetch.getTodayForecast(getActivity(), lat, lon);
                final JSONObject jsonForecast = RemoteFetch.getFiveDayForecast(getActivity(), lat, lon);
                if (jsonCurrent == null && jsonForecast == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "GPS Not Enabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            renderCurrentWeather(jsonCurrent);
                            Log.d("JSONFORECAST", jsonForecast.toString());
                            renderForecastWeather(jsonForecast);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderCurrentWeather(JSONObject json) {
        try {
            JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            mName.setText(json.getString("name"));
            mWeather.setText(weather.getString("description"));
            mTemp.setText(main.getLong("temp") + "" + (char) 0x00B0);
            mTempHigh.setText("MAX: " + main.getLong("temp_max") + "" + (char) 0x00B0);
            mTempLow.setText("MIN: " + main.getLong("temp_min") + "" + (char) 0x00B0);
            mHumidity.setText("Humidity " + main.getString("humidity") + "%");
            setWeatherIcon(weather.getInt("id"), json.getJSONObject("sys").getLong("sunrise") * 1000, json.getJSONObject("sys").getLong("sunset") * 1000);
        } catch (JSONException e) {
            Log.e("CURRENT_JSON_ERROR", e.toString());
        }
    }

    private void renderForecastWeather(JSONObject json) {
        try {
            arrayListForecast.clear(); // clear list, prevent duplicates on refresh
            JSONArray list = json.getJSONArray("list");
            for (int i = 0; i < 6; i++) {
                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");
                Double temp = main.getDouble("temp");
                JSONObject weather = listItem.getJSONArray("weather").getJSONObject(0);
                Forecast forecast = new Forecast();
                forecast.setHighTemp(String.valueOf(main.getDouble("temp_max")));
                forecast.setLowTemp(String.valueOf(main.getDouble("temp_min")));
                forecast.setWeather(weather.get("description").toString());
                forecast.setWeatherId(weather.get("id").toString());
                arrayListForecast.add(forecast);
            }
            ForecastAdapter testAdapter = new ForecastAdapter(getActivity(), 0, arrayListForecast);
            mListViewForecast.setAdapter(testAdapter);
            SetListViewHeight.setListViewHeight(mListViewForecast);
        } catch (JSONException e) {
            Log.e("FORECAST_JSON_ERROR", e.toString());
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = getString(R.string.weather_rainy);
                    break;
            }
        }
        mWeatherIcon.setText(icon);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateWeather(location.getLatitude() + "", location.getLongitude() + "");
    }

    /**
     * Not being used
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
