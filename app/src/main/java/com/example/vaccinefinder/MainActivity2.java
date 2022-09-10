package com.example.vaccinefinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.Utils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity2 extends AppCompatActivity {

    int LOCATION_PERMISSION_CODE = 1;
    LocationManager locationManager;

    double latitude, longitude;

    public static final int RETURN_VALUE = 1;
    public static final String INTENT_CODE = "KEY_1";

    ArrayList<MainActivity.ActNow> parcelableArrayList;

    static int MAX_Y_VALUE;
    static int MIN_Y_VALUE;
    static String SET_LABEL = "";
    static String[] CATS = {"", "", "", ""};
    BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        parcelableArrayList = getIntent().getParcelableArrayListExtra("ARRAY_LIST");

        Log.d("TAG4", String.valueOf(parcelableArrayList.get(0).getPopulation()));

        TextView population = findViewById(R.id.population);
        TextView vaxx_initiate = findViewById(R.id.vaxx_initiate);
        TextView cases = findViewById(R.id.cases);
        TextView deaths = findViewById(R.id.deaths);
        TextView recent_cases = findViewById(R.id.recentCases);
        TextView recent_deaths = findViewById(R.id.recentDeaths);
        TextView state_county = findViewById(R.id.state_county);
        TextView percent_vaxx = findViewById(R.id.percentVaxx);
        population.setText("Population: "+ parcelableArrayList.get(0).getPopulation());
        vaxx_initiate.setText("Vaccines Initiated: "+parcelableArrayList.get(0).getVaccines_initiated());
        cases.setText("Cases: "+parcelableArrayList.get(0).getCases());
        deaths.setText("Deaths: "+parcelableArrayList.get(0).getDeaths());
        recent_cases.setText("Recent Cases: "+parcelableArrayList.get(0).getRecent_cases());
        recent_deaths.setText("Recent Deaths: "+parcelableArrayList.get(0).getRecent_deaths());
        state_county.setText(parcelableArrayList.get(0).getState_county());

        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        double vaxxToPop = ((double)parcelableArrayList.get(0).getVaccines_initiated()) / ((double)parcelableArrayList.get(0).getPopulation());
        percent_vaxx.setText("Percent of Population Vaccinated: "+ Math.round(vaxxToPop*100) +"%");
        if (vaxxToPop>=.40) constraintLayout.setBackgroundColor(Color.rgb(150, 255, 163));
        else if (vaxxToPop>=.35) constraintLayout.setBackgroundColor(Color.rgb(192, 255, 156));
        else if (vaxxToPop>=.30) constraintLayout.setBackgroundColor(Color.rgb(229, 255, 156));
        else if (vaxxToPop>=.25) constraintLayout.setBackgroundColor(Color.rgb(255, 252, 156));
        else if (vaxxToPop>=.2) constraintLayout.setBackgroundColor(Color.rgb(255, 219, 156));
        else constraintLayout.setBackgroundColor(Color.rgb(255, 166, 166));

        MAX_Y_VALUE = parcelableArrayList.get(0).getPopulation();
        MIN_Y_VALUE = parcelableArrayList.get(0).getDeaths();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.d("TAG", "VACCINE INITIATE: "+String.valueOf(parcelableArrayList.get(0).getVaccines_initiated()));

        for (int i = 0; i<parcelableArrayList.size(); i++){
            Log.d("TAG2", String.valueOf(parcelableArrayList.size()));
            Log.d("TAG2", "POPULATION OF LOCATION: "+String.valueOf(parcelableArrayList.get(i).getPopulation()));
        }

        chart = findViewById(R.id.fragment_verticalbarchart_chart);

        BarData data = createChartData();
        configureChartAppearance();
        prepareChartData(data);

        Button go_vaxx = findViewById(R.id.go_vaxx);
        go_vaxx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendInfoBack = new Intent(MainActivity2.this, MainActivity.class);
                sendInfoBack.putExtra(INTENT_CODE, parcelableArrayList.get(0).getLatitude1());
                sendInfoBack.putExtra(INTENT_CODE, parcelableArrayList.get(0).getLongitude1());
                setResult(RESULT_OK, sendInfoBack);
                finish();

            }
        });

    }

    private void configureChartAppearance() {
        chart.getDescription().setEnabled(false);
        chart.setDrawValueAboveBar(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return CATS[(int) value];
            }
        });

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setGranularity(30f);
        axisLeft.setAxisMinimum(0);

        YAxis axisRight = chart.getAxisRight();
        axisRight.setGranularity(30f);
        axisRight.setAxisMinimum(0);
    }

    private BarData createChartData() {
        ArrayList<BarEntry> values = new ArrayList<>();
        values.add(new BarEntry(0, parcelableArrayList.get(0).getPopulation()));
        values.add(new BarEntry(1, parcelableArrayList.get(0).getVaccines_initiated()));
        values.add(new BarEntry(2, parcelableArrayList.get(0).getCases()));
        values.add(new BarEntry(3, parcelableArrayList.get(0).getDeaths()));

        BarDataSet set1 = new BarDataSet(values, SET_LABEL);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);

        return data;
    }

    private void prepareChartData(BarData data) {
        data.setValueTextSize(12f);
        chart.setData(data);
        chart.invalidate();
    }

}
