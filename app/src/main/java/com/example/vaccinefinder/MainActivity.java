package com.example.vaccinefinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.example.vaccinefinder.MainActivity2.INTENT_CODE;
import static com.example.vaccinefinder.MainActivity2.RETURN_VALUE;

public class MainActivity extends AppCompatActivity implements LocationListener{

    int LOCATION_PERMISSION_CODE = 1;

    double latitude, longitude;
    static Location userLocation = new Location("");
    static Location oldUserLocation = null;

    ArrayList<Covid> covidArrayList = new ArrayList<Covid>();
    ArrayList<ActNow> actNowArrayList = new ArrayList<ActNow>();

    JSONObject vaccineAPI = new JSONObject();

    ListView listView;
    Button stats_button;
    Button loc_button;
    ProgressBar progressBar;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);

        loc_button = findViewById(R.id.loc_button);
        loc_button.setEnabled(false);
        loc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldUserLocation!=userLocation){
                    listView.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    stats_button.setEnabled(false);
                }
                oldUserLocation = userLocation;
                new Content().execute(String.valueOf(userLocation.getLatitude()), String.valueOf(userLocation.getLongitude()));
            }
        });

        stats_button = findViewById(R.id.stats_button);
        stats_button.setEnabled(false);
        stats_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putParcelableArrayListExtra("ARRAY_LIST", actNowArrayList);
                intent.putExtra(INTENT_CODE, actNowArrayList);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        actNowArrayList.clear();
        covidArrayList.clear();
        Log.d("TAG", String.valueOf("Latitude: "+location.getLatitude()));
        Log.d("TAG", String.valueOf("Longitude: "+location.getLongitude()));
        if (oldUserLocation==null) Toast.makeText(MainActivity.this, "Press the button to begin", Toast.LENGTH_LONG).show();
        loc_button.setEnabled(true);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        userLocation = location;
        if (oldUserLocation!=null){
            if (userLocation!=oldUserLocation)
                loc_button.setEnabled(true);
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    class Content extends AsyncTask<String, Covid, JSONObject>{

        JSONObject fipsAPI = new JSONObject();
        JSONObject actNowAPI = new JSONObject();
        String actNowAPI_ID;

        protected void onPreExecute() {
            super.onPreExecute();
            actNowAPI_ID = "a82d810ed701437a9898a77f5a5308c5";

        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            URLConnection urlConnection1 = null;
            URLConnection urlConnection2 = null;
            URLConnection urlConnection3 = null;

            try {
                URL url1 = new URL ("https://geo.fcc.gov/api/census/block/find?latitude="+strings[0]+"&longitude="+strings[1]+"&format=json");
                Log.d("TAG", (url1.toString()));
                urlConnection1 = url1.openConnection();
                InputStream inputStream1 = urlConnection1.getInputStream();
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1));
                String s1 = bufferedReader1.readLine();
                fipsAPI = new JSONObject(s1);

                String fips = fipsAPI.getJSONObject("Block").getString("FIPS");
                String fips2 = "";
                for (int i = 0; i<5; i++)
                    fips2+=fips.charAt(i);
                Log.d("TAG", fips2);
                URL url2 = new URL("https://api.covidactnow.org/v2/county/"+fips2+".json?apiKey="+actNowAPI_ID);
                Log.d("TAG", (url2.toString()));
                urlConnection2 = url2.openConnection();
                InputStream inputStream2 = urlConnection2.getInputStream();
                BufferedReader bufferedReader2 = new BufferedReader(new InputStreamReader(inputStream2));
                String s2 = bufferedReader2.readLine();
                actNowAPI = new JSONObject(s2);

                Log.d("TAG2", String.valueOf(actNowAPI.getJSONObject("actuals").getInt("deaths")));

                int population = actNowAPI.getInt("population");
                int vaccines_initiated = actNowAPI.getJSONObject("actuals").getInt("vaccinationsInitiated");
                int cases = actNowAPI.getJSONObject("actuals").getInt("cases");
                int deaths = actNowAPI.getJSONObject("actuals").getInt("deaths");
                Log.d("TAG4", String.valueOf(+deaths));
                int new_cases = actNowAPI.getJSONObject("actuals").getInt("newCases");
                int new_deaths = actNowAPI.getJSONObject("actuals").getInt("newDeaths");
                String state_county = actNowAPI.getString("state") + ": " + actNowAPI.getString("county");

                actNowArrayList.add(new ActNow(population, vaccines_initiated, cases, deaths, new_cases, new_deaths, userLocation.getLatitude(), userLocation.getLongitude(), state_county));

                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putParcelableArrayListExtra("ARRAY_LIST", actNowArrayList);
                intent.putExtra(INTENT_CODE, actNowArrayList);

                Log.d("TAG2", "CHECK");
                Log.d("TAG2", String.valueOf(actNowAPI.getJSONObject("actuals").getInt("vaccinationsInitiated")));

                String state = fipsAPI.getJSONObject("State").getString("code");
                Log.d("TAG", state);
                URL url3 = new URL ("https://www.vaccinespotter.org/api/v0/states/"+state+".json");
                Log.d("TAG", (url3.toString()));
                urlConnection3 = url3.openConnection();
                InputStream inputStream3 = urlConnection3.getInputStream();
                BufferedReader bufferedReader3 = new BufferedReader(new InputStreamReader(inputStream3));
                String s3 = bufferedReader3.readLine();
                vaccineAPI = new JSONObject(s3);

                Log.d("TAG", String.valueOf(vaccineAPI.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")));

                for (int i = 0; i<vaccineAPI.getJSONArray("features").length(); i++){
                    //Log.d("TAG", String.valueOf(vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)));
                    Log.d("TAG", String.valueOf(i));

                    Location targetLocation = new Location("");
                    targetLocation.setLongitude(vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(0));
                    targetLocation.setLatitude(vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates").getDouble(1));

                    //Log.d("TAG", targetLocation.toString());
                    //Log.d("TAG", userLocation.toString());
                    Log.d("TAG", "HAS APPOINTMENT: "+ String.valueOf(vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("appointments_available")));
                    Log.d("TAG", "CARRIES VACCINE: "+ String.valueOf(vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("carries_vaccine")));

                    if (vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("appointments_available").equalsIgnoreCase("true")){
                        if (vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("carries_vaccine").equalsIgnoreCase("true")){

                            String city = vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("city");
                            String name = vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("name");
                            String address = vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("address");
                            String brand = vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("provider_brand_name");
                            float distance = ((userLocation.distanceTo(targetLocation))/1609);
                            String url = vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("url");
                            boolean p = false;
                            boolean m = false;
                            boolean j = false;

                            //Log.d("TAG", "PFIZER: "+ String.valueOf(vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getJSONObject("appointment_vaccine_types").getString("pfizer")));

                            try {
                                if (vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getJSONObject("appointment_vaccine_types").getString("pfizer").equalsIgnoreCase("true"))
                                    p = true;
                                if (vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getJSONObject("appointment_vaccine_types").getString("moderna").equalsIgnoreCase("true"))
                                    m = true;
                                if (vaccineAPI.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getJSONObject("appointment_vaccine_types").getString("jj").equalsIgnoreCase("true"))
                                    j = true;
                            }catch(Exception e){}

                            covidArrayList.add(new Covid(city, name, address, brand, distance, url, p, m, j));

                        }
                    }

                }

                for (int j = 0; j<covidArrayList.size()-1; j++){
                    int MIN_INDEX = j;
                    for (int k = j+1; k<covidArrayList.size(); k++)
                        if (covidArrayList.get(k).getDistance() < covidArrayList.get(MIN_INDEX).getDistance())
                            MIN_INDEX = k;
                    float temp1 = covidArrayList.get(MIN_INDEX).getDistance();
                    covidArrayList.get(MIN_INDEX).setDistance(covidArrayList.get(j).getDistance());
                    covidArrayList.get(j).setDistance(temp1);
                }

                Log.d("TAG", "Complete");

                for (int i = 0; i<covidArrayList.size(); i++){
                    Log.d("TAG", covidArrayList.get(i).getCity());
                    Log.d("TAG", covidArrayList.get(i).getName());
                    Log.d("TAG", covidArrayList.get(i).getAddress());
                    Log.d("TAG", covidArrayList.get(i).getBrand());
                    Log.d("TAG", String.valueOf(covidArrayList.get(i).getDistance()));
                    Log.d("TAG", covidArrayList.get(i).getUrl());
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            Log.d("TAG", "Entering ListAdapter");
            ListAdapter listAdapter = new ListAdapter(MainActivity.this, R.layout.adapter_list, covidArrayList);
            listView.setAdapter(listAdapter);

        }
    }

    public class ListAdapter extends ArrayAdapter<Covid>{

        Context mainContext;
        int xml;
        List<Covid> list;

        public ListAdapter(@NonNull Context context, int resource, @NonNull List<Covid> objects) {
            super(context, resource, objects);
            mainContext = context;
            xml = resource;
            list = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            LayoutInflater layoutInflater = (LayoutInflater) mainContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View adapterView = layoutInflater.inflate(xml, null);

            TextView adapter_city = adapterView.findViewById(R.id.adapter_city);
            adapter_city.setText("City: "+list.get(position).getCity());
            TextView adapter_name = adapterView.findViewById(R.id.adapter_name);
            adapter_name.setText("Name: "+list.get(position).getName());
            TextView adapter_address = adapterView.findViewById(R.id.adapter_address);
            adapter_address.setText("Address: "+list.get(position).getAddress());
            TextView adapter_brand = adapterView.findViewById(R.id.adapter_brand);
            adapter_brand.setText("Brand: "+list.get(position).getBrand());
            TextView adapter_distance = adapterView.findViewById(R.id.adapter_distance);
            adapter_distance.setText("Distance: "+String.valueOf(list.get(position).getDistance())+" miles");

            ImageView adapter_vaxImage = adapterView.findViewById(R.id.adapter_vaxImage);

            if (list.get(position).isJj()){
                if (list.get(position).isModerna()) adapter_vaxImage.setImageResource(R.drawable.moderna_jj);
                else if (list.get(position).isPfizer()) adapter_vaxImage.setImageResource(R.drawable.pfizer_jj);
                else adapter_vaxImage.setImageResource(R.drawable.jj);
            }
            if (list.get(position).isModerna()){
                if (list.get(position).pfizer) adapter_vaxImage.setImageResource(R.drawable.pfizer_moderna);
                else adapter_vaxImage.setImageResource(R.drawable.moderna);
            }
            else if (list.get(position).isPfizer()) adapter_vaxImage.setImageResource(R.drawable.pfizer);
            else adapter_vaxImage.setImageResource(R.drawable.generic);

            if (list.get(position).getName().toLowerCase().contains("moderna")) adapter_vaxImage.setImageResource(R.drawable.moderna);
            else if (list.get(position).getName().toLowerCase().contains("pfizer")) adapter_vaxImage.setImageResource(R.drawable.pfizer);
            else if (list.get(position).getName().toLowerCase().contains("j&j")) adapter_vaxImage.setImageResource(R.drawable.jj);

            adapter_vaxImage.setClickable(true);
            adapter_vaxImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(list.get(position).getUrl()));
                    startActivity(intent);
                }
            });

            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            stats_button.setEnabled(true);
            loc_button.setEnabled(false);
            return adapterView;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("TAG", "Acknowledged.");

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d("TAG", "Acknowledged 2.");
                        locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, 400, 1, MainActivity.this);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your Location", Toast.LENGTH_SHORT).show();
                }

                return;

            }
        }

    }

    public static class Covid implements Parcelable{

        String city;
        String name;
        String address;
        String brand;
        float distance;
        String url;
        boolean pfizer, moderna, jj;

        public Covid (String city, String name, String address, String brand, float distance, String url, boolean pfizer, boolean moderna, boolean jj){
            this.city = city;
            this.name = name;
            this.address = address;
            this.brand = brand;
            this.distance = distance;
            this.url = url;
            this.pfizer = pfizer;
            this.moderna = moderna;
            this.jj = jj;

        }

        protected Covid(Parcel in) {
            city = in.readString();
            name = in.readString();
            address = in.readString();
            brand = in.readString();
            distance = in.readFloat();
            url = in.readString();
            pfizer = in.readByte() != 0;
            moderna = in.readByte() != 0;
            jj = in.readByte() != 0;
        }

        public static final Creator<Covid> CREATOR = new Creator<Covid>() {
            @Override
            public Covid createFromParcel(Parcel in) {
                return new Covid(in);
            }

            @Override
            public Covid[] newArray(int size) {
                return new Covid[size];
            }
        };

        public String getCity() {
            return city;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getBrand() {
            return brand;
        }

        public float getDistance() {
            return distance;
        }

        public void setDistance(float distance){
            this.distance = distance;
        }

        public String getUrl() {
            return url;
        }

        public boolean isJj() {
            return jj;
        }

        public boolean isModerna() {
            return moderna;
        }

        public boolean isPfizer() {
            return pfizer;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(city);
            dest.writeString(name);
            dest.writeString(address);
            dest.writeString(brand);
            dest.writeFloat(distance);
            dest.writeString(url);
            dest.writeByte((byte) (pfizer ? 1 : 0));
            dest.writeByte((byte) (moderna ? 1 : 0));
            dest.writeByte((byte) (jj ? 1 : 0));
        }
    }

    public static class ActNow implements Parcelable {

        int population, vaccines_initiated, cases, deaths, recent_cases, recent_deaths;
        double latitude1, longitude1;
        String state_county;

        public ActNow(int population, int vaccines_initiated, int cases, int deaths, int recent_cases, int recent_deaths, double latitude1, double longitude1, String state_county){
            this.population = population;
            this.vaccines_initiated = vaccines_initiated;
            this.cases = cases;
            this.deaths = deaths;
            this.recent_cases = recent_cases;
            this.recent_deaths = recent_deaths;
            this.latitude1 = latitude1;
            this.longitude1 = longitude1;
            this.state_county = state_county;

        }

        public int getPopulation() {
            return population;
        }

        public int getVaccines_initiated() {
            return vaccines_initiated;
        }

        public int getCases() {
            return cases;
        }

        public int getDeaths() {
            return deaths;
        }

        public int getRecent_cases() {
            return recent_cases;
        }

        public int getRecent_deaths() {
            return recent_deaths;
        }

        public double getLatitude1() {
            return latitude1;
        }

        public double getLongitude1() {
            return longitude1;
        }

        public String getState_county() {
            return state_county;
        }

        protected ActNow(Parcel in) {
            population = in.readInt();
            vaccines_initiated = in.readInt();
            cases = in.readInt();
            deaths = in.readInt();
            recent_cases = in.readInt();
            recent_deaths = in.readInt();
            latitude1 = in.readDouble();
            longitude1 = in.readDouble();
            state_county = in.readString();
        }

        public static final Creator<ActNow> CREATOR = new Creator<ActNow>() {
            @Override
            public ActNow createFromParcel(Parcel in) {
                return new ActNow(in);
            }

            @Override
            public ActNow[] newArray(int size) {
                return new ActNow[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(population);
            dest.writeInt(vaccines_initiated);
            dest.writeInt(cases);
            dest.writeInt(deaths);
            dest.writeInt(recent_cases);
            dest.writeInt(recent_deaths);
            dest.writeDouble(latitude1);
            dest.writeDouble(longitude1);
            dest.writeString(state_county);
        }
    }

}