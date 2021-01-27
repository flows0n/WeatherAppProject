package com.example.weatherappproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.provider.Telephony.Mms.Part.TEXT;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CITY = "";

    private AlertDialog.Builder searchCityBuilder;
    private AlertDialog searchCity;

    private AlertDialog.Builder infoDialogBuilder;
    private AlertDialog infoDialog;

    private EditText searchCityPopUp_city;

    public String actualCity;
    public String set1;
    public String set2;
    public String set3;
    public boolean connected = false;
    FusedLocationProviderClient fusedLocationProviderClient;

    ImageView imageWeather;
    TextView label_city;
    TextView label_main;
    TextView label_temp;
    TextView label_pressure;
    TextView label_humidity;
    TextView label_wind;

    private Button save;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState( Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CITY, actualCity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        if (haveNetwork()){
            api_key(actualCity);
        } else if (!haveNetwork()) {
            Toast.makeText(MainActivity.this, getText(R.string.networkUnable), Toast.LENGTH_SHORT).show();
        }

    }

    private boolean haveNetwork(){
        boolean have_WIFI= false;
        boolean have_MobileData = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        for(NetworkInfo info:networkInfos){
            if (info.getTypeName().equalsIgnoreCase("WIFI"))if (info.isConnected())have_WIFI=true;
            if (info.getTypeName().equalsIgnoreCase("MOBILE DATA"))if (info.isConnected())have_MobileData=true;
        }
        return have_WIFI||have_MobileData;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        label_city = findViewById(R.id.town);
        label_temp = findViewById(R.id.temp);
        label_main = findViewById(R.id.info);
        label_pressure = findViewById(R.id.desc1);
        label_humidity = findViewById(R.id.desc2);
        label_wind = findViewById(R.id.desc3);
        imageWeather = findViewById(R.id.weather_image);

        if (savedInstanceState != null){
            actualCity = savedInstanceState.getString(KEY_CITY);
            if (haveNetwork()){
                api_key(actualCity);
            } else if (!haveNetwork()) {
                Toast.makeText(MainActivity.this, getText(R.string.networkUnable), Toast.LENGTH_SHORT).show();
            }
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                if (haveNetwork()){
                    api_key(actualCity);
                } else if (!haveNetwork()) {
                    Toast.makeText(MainActivity.this, getText(R.string.networkUnable), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.local: {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                    if (haveNetwork()){
                        api_key(actualCity);
                    } else if (!haveNetwork()) {
                        Toast.makeText(MainActivity.this, getText(R.string.networkUnable), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
                return true;
            }
            case R.id.searchCity: {
                createNewSearchCityDialog();
                return true;
            }
            case R.id.settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.about:
                createAllertDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAllertDialog() {
        infoDialogBuilder = new AlertDialog.Builder(this);
        final View infoDialogView = getLayoutInflater().inflate(R.layout.infopopup, null);
        TextView b =  infoDialogView.findViewById(R.id.textView3);

        infoDialogBuilder.setView(infoDialogView);
        infoDialog = infoDialogBuilder.create();
        infoDialog.show();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/flows0n/WeatherAppProject");
                Intent internet = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(internet);
            }
        });
    }

    public void createNewSearchCityDialog() {
        searchCityBuilder = new AlertDialog.Builder(this);
        final View searchCityPopupView = getLayoutInflater().inflate(R.layout.searchpopup, null);
        searchCityPopUp_city = (EditText) searchCityPopupView.findViewById(R.id.popupcity);

        save = (Button) searchCityPopupView.findViewById(R.id.button);

        searchCityBuilder.setView(searchCityPopupView);
        searchCity = searchCityBuilder.create();
        searchCity.show();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchCityPopUp_city.onEditorAction(EditorInfo.IME_ACTION_DONE);
                if (haveNetwork()){
                    api_key(String.valueOf(searchCityPopUp_city.getText()));
                } else if (!haveNetwork()) {
                    Toast.makeText(MainActivity.this, getText(R.string.networkUnable), Toast.LENGTH_SHORT).show();
                }
                actualCity = String.valueOf(searchCityPopUp_city.getText());
                searchCity.cancel();
            }
        });
    }


    private void api_key(final String City) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        set1 = sharedPreferences.getString("temp", "");
        set2 = sharedPreferences.getString("press", "");
        set3 = sharedPreferences.getString("wind", "");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/weather?q=" + City + "&appid=a918c4f706f2f119ca1232c10dcea980&units=metric")
                .get()
                .build();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseData = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseData);
                    JSONArray array = json.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);

                    String description = object.getString("main");
                    String icons = object.getString("icon");

                    JSONObject temp1 = json.getJSONObject("main");
                    Double temperature = temp1.getDouble("temp");
                    Double pressure = temp1.getDouble("pressure");
                    Double humidity = temp1.getDouble("humidity");

                    JSONObject temp2 = json.getJSONObject("wind");
                    Double windSpeed = temp2.getDouble("speed");

                    String temps;
                    String press;
                    String wind;
                    setText(label_city, City);
                    switch(set1){
                        case "1":
                            temps = Math.round(temperature) + " °C";
                            break;
                        case "2":
                            temperature = (temperature *1.8) + 32;
                            temps = Math.round(temperature) + " °F";
                            break;
                        default:
                            temps =Math.round(temperature) + " °C";
                            break;

                    }
                    switch(set2){
                        case "1":
                            press = Math.round(pressure) + " hPA";
                            break;
                        case "2":
                            press = Math.round(pressure) + " mbar";
                            break;
                        default:
                            press = Math.round(pressure) + " hPA";
                            break;
                    }

                    switch(set3){
                        case "1":
                            wind = Math.round(windSpeed) + " m/s";
                            break;
                        case "2":
                            windSpeed = windSpeed *3.6;
                            wind = Math.round(windSpeed) + " km/h";
                            break;
                        case "3":
                            windSpeed = (windSpeed *3.6)/1.609344;
                            wind = Math.round(windSpeed) + " mph";
                            break;
                        default:
                            wind =wind = Math.round(windSpeed) + " m/s";;
                            break;
                    }
                    String humi = Math.round(humidity) + " %";

                    setText(label_temp, getString(R.string.temperature) + " " + temps);
                    setText(label_main, description);
                    setText(label_pressure, getString(R.string.pressure) + " " + press);
                    setText(label_humidity, getString(R.string.huminity) + " " + humi);
                    setText(label_wind, getString(R.string.windSpeed) + " " + wind);
                    setImage(imageWeather, icons);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
        actualCity = City;
        saveData();
    }

    private void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    private void setImage(final ImageView imageView, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //paste switch
                switch (value) {
                    case "01d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.d01d));
                        break;
                    case "01n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.n01n));
                        break;
                    case "02d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.d02d));
                        break;
                    case "02n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.n02n));
                        break;
                    case "03d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn03));
                        break;
                    case "03n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn03));
                        break;
                    case "04d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn04));
                        break;
                    case "04n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn04));
                        break;
                    case "09d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn09));
                        break;
                    case "09n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn09));
                        break;
                    case "10d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.d10d));
                        break;
                    case "10n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.n10n));
                        break;
                    case "11d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn11));
                        break;
                    case "11n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn11));
                        break;
                    case "13d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.d13d));
                        break;
                    case "13n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.n13n));
                        break;
                    case "50d":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn50));
                        break;
                    case "50n":
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.dn50));
                        break;
                    default:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.question));

                }
            }
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> adresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        actualCity = adresses.get(0).getLocality();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(KEY_CITY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT, actualCity);
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(KEY_CITY, MODE_PRIVATE);
        actualCity = sharedPreferences.getString(TEXT, "");
    }

}
