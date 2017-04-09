package pe.edu.upc.mybestday;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String locationProvider = LocationManager.GPS_PROVIDER;
    LocationManager locationManager;
    LocationListener locationListener;
    final static int PERMISSIONS_REQUEST_ACCESS_LOCATION = 100;
    boolean locationPermissionGranted = false;

    // URL Wheater
    private static String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?lat=LATITUD&lon=LONGITUD&appid=605f81f9ff5734c04b0edfacea9dbfa2&lang=es&units=metric";
    private static String COUNTRY_URL = "https://restcountries.eu/rest/v2/alpha/";
    private static String ICON_URL = "http://openweathermap.org/img/w/";

    private Context myContext;
    private Weather weather;
    private double latitude;
    private double longitude;

    //UI
    TextView countryTextView;
    TextView wheaterTextView;
    TextView tempTextView;
    TextView humidityTextView;
    TextView pressureTextView;
    TextView tempMinTextView;
    TextView tempMaxTextView;
    TextView ciyTextView;
    ImageView weatherImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupLocationUpdates();
        WEATHER_URL = WEATHER_URL.replace("LONGITUD",String.valueOf(longitude));
        WEATHER_URL = WEATHER_URL.replace("LATITUD",String.valueOf(latitude));

        Log.d("WEATHER_URL : ", WEATHER_URL);

        countryTextView = (TextView) findViewById(R.id.countryTextView);
        wheaterTextView = (TextView) findViewById(R.id.wheaterTextView);
        tempTextView = (TextView) findViewById(R.id.tempTextView);
        humidityTextView = (TextView) findViewById(R.id.humidityTextView);
        pressureTextView = (TextView) findViewById(R.id.pressureTextView);
        tempMinTextView = (TextView) findViewById(R.id.tempMinTextView);
        tempMaxTextView = (TextView) findViewById(R.id.tempMaxTextView);
        ciyTextView = (TextView) findViewById(R.id.ciyTextView);
        weatherImageView = (ImageView) findViewById(R.id.weatherImageView);

        myContext = this.getBaseContext();
        searchWeather(WEATHER_URL);

    }

    private void validatePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_LOCATION);
            }
        } else {
            locationPermissionGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    locationPermissionGranted = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    locationPermissionGranted = false;
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void refreshCurrentLocation(Location location) {
        String locationDescription = "Latitude: " +
                String.valueOf(location.getLatitude()) + " Longitude: " +
                String.valueOf(location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("Coordenadas : ", locationDescription);
    }

    private void setupLocationUpdates() {
        validatePermissions();
        if (locationPermissionGranted) {
            locationManager = (LocationManager)
                    this.getSystemService(Context.LOCATION_SERVICE);
            // Define a listener that responds to location updates
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    refreshCurrentLocation(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
            refreshCurrentLocation(locationManager.getLastKnownLocation(locationProvider));
        }
    }

    public void searchWeather(String searchWeatherUrl) {
        System.out.println("URL = " + searchWeatherUrl);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, searchWeatherUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            JSONArray resultsArray = response.getJSONArray("weather");
                            weather = new Weather();
                            JSONObject json = resultsArray.getJSONObject(0);
                            weather.setId(json.optInt("id"));
                            weather.setMain(json.getString("main"));
                            weather.setDescription(json.getString("description"));
                            weather.setIcon(json.getString("icon"));

                            json = response.getJSONObject("main");

                            weather.setTemp(json.getString("temp"));
                            weather.setPressure(json.getString("pressure"));
                            weather.setHumidity(json.getString("humidity"));
                            weather.setTemp_min(json.getString("temp_min"));
                            weather.setTemp_max(json.getString("temp_max"));

                            json = response.getJSONObject("sys");

                            weather.setCountry(json.getString("country"));

                            weather.setName(response.getString("name"));

                            searchCountry(COUNTRY_URL + weather.getCountry());

                            ciyTextView.setText(weather.getName());
                            wheaterTextView.setText(weather.getDescription());
                            tempTextView.setText(weather.getTemp());
                            humidityTextView.setText(weather.getHumidity());
                            pressureTextView.setText(weather.getPressure());
                            tempMinTextView.setText(weather.getTemp_min());
                            tempMaxTextView.setText(weather.getTemp_max());

                            Picasso.with(myContext).load(ICON_URL + weather.getIcon() + ".png").into(weatherImageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );
        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void searchCountry(String searchCountryUrl) {
        System.out.println("URL = " + searchCountryUrl);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET, searchCountryUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            JSONObject json = response.getJSONObject("translations");

                            weather.setCountryName(json.getString("es"));
                            countryTextView.setText(weather.getCountryName());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );
        Volley.newRequestQueue(this).add(jsonRequest);
    }
}
