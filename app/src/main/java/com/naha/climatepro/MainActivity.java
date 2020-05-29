package com.naha.climatepro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends Activity {

    private static final String TAG = "CLIMATE PRO";
    // Constants:
    final int REQUEST_CODE = 1;
//    http://api.openweathermap.org/data/2.5/forecast?id=524901&APPID={008a103d0d817fa04fafc83c26c75092}
//    api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={your api key}
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "008a103d0d817fa04fafc83c26c75092";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:

    String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;//coarse location
//    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;//fine location


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    TextView mWeatherName;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager locationManager;
    LocationListener locationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChangeCityActivity.class);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        Log.d(TAG, "Getting weather for current location");
        Intent myIntent = getIntent();
        String CityRequested = myIntent.getStringExtra("city");

        if (CityRequested != null){
            //request weather for city
            getWeatherForNewCity(CityRequested);
        }else{
            getWeatherForCurrentLocation();
        }



    }
    private void getWeatherForNewCity(String cityRequested) {
        RequestParams requestParamsByCity = new RequestParams();
        requestParamsByCity.put("q",cityRequested);
        requestParamsByCity.put("appid",APP_ID);
        queryAPINow(requestParamsByCity);
    }

    private void getWeatherForCurrentLocation() {
        //get instance of the location manager service.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //create an instance of the location listener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged: Location changed callback");

                //extract location data
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationChanged: lat = "+latitude+", long. = "+longitude);

                //Create request params binding
                RequestParams requestParams = new RequestParams();
                requestParams.put("lat",latitude);
                requestParams.put("lon", longitude);
                requestParams.put("appid",APP_ID);

                queryAPINow(requestParams);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled: Location Service is disabled in the device");

            }
        };

        // instruct location manager to request updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 1 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG, "onRequestPermissionsResult: Permission  granted");
                getWeatherForCurrentLocation();
            }
            else
            {
                Log.d(TAG, "onRequestPermissionsResult: Permission denied");

            }

        }
    }
    WeatherDataModel dataModel;


    public void queryAPINow(RequestParams requestParams)
    {
        //create a http client
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        asyncHttpClient.get(WEATHER_URL,requestParams,new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, "onSuccess: Successfully pulled data"+response.toString());

                dataModel =WeatherDataModel.fromJSON(response);
                updateUI();
                Log.d(TAG, "onSuccess: "+dataModel.getmCity());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Log.d(TAG, "onFailure: Failed "+throwable.toString());
                Toast.makeText(MainActivity.this, "Failed to query weather updates, please check aagin",Toast.LENGTH_SHORT).show();
            }
        });

    }


    protected void updateUI()
    {
        mCityLabel.setText(dataModel.getmCity());
        Log.w(TAG, "updateUI: locale"+mCityLabel.getText());
        mTemperatureLabel.setText(dataModel.getmTemperature());

        int imgResource = getResources().getIdentifier(dataModel.getmIconName(),/*dir*/"drawable",getPackageName());
        mWeatherImage.setImageResource(imgResource);
        mWeatherName = (TextView)findViewById(R.id.weatherName);
        String iconName = dataModel.getmIconName();
        if (iconName == "tstorm1") {
            mWeatherName.setText("thunderstorm with rain");
        } else if (iconName.equals("light_rain")) {
            mWeatherName.setText("light rains");
        } else if (iconName.equals("shower3")) {
            mWeatherName.setText("heavy rains");
        } else if (iconName.equals("snow4")) {
            mWeatherName.setText("snowing");
        } else if (iconName.equals("fog")) {
            mWeatherName.setText("foggy");
        } else if (iconName.equals("tstorm3")) {
            mWeatherName.setText("thunderstorm with rain");
        } else if (iconName.equals("sunny")) {
            mWeatherName.setText("hot and sunny");
        } else if (iconName.equals("cloudy2")) {
            mWeatherName.setText("overcast");
        } else if (iconName.equals("snow5")) {
            mWeatherName.setText("heavy shower snow");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //free data
        if (locationManager != null) locationManager.removeUpdates(locationListener);

    }
}
