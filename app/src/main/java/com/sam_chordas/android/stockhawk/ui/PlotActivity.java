package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PlotActivity extends AppCompatActivity{

    LineChart lineChart;
    AQuery aQuery;
    String symbol;
    String downloadedJson;
    final String BASE_URL = "http://query.yahooapis.com/v1/public/yql?q=";
    final String URL_QUALIFIERS = "&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&format=json";
    String query;
    Time time;
    int month;
    int prevMonth;
    String monthString;
    String prevMonthString;
    int date;
    int prevMonthsDate;
    int year;
    int prevMonthsYear;
    ArrayList<String> bidPrice;
    ArrayList<String> dateForPlot;

    boolean isConnected = false;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        symbol = getIntent().getStringExtra("symbol");

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected)
            Toast.makeText(this, R.string.network_warning, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();

        aQuery = new AQuery(this);

        dateForPlot = new ArrayList<String>();
        bidPrice = new ArrayList<String>();


        time = new Time();
        time.setToNow();

        month = time.month + 1;
        date = time.monthDay;
        year = time.year;

        if(date >= 28)
            prevMonthsDate = 1;
        else
            prevMonthsDate = date + 1;

        if(month == 1) {
            if(prevMonthsDate != 1) {
                prevMonthsYear = year - 1;
                prevMonth = 12;
            }else{
                prevMonthsYear = year;
                prevMonth = 1;
            }
        }
        else {
            prevMonthsYear = year;
            if(prevMonthsDate != 1)
                prevMonth = month - 1;
            else
                prevMonth = month;
        }


        if(month < 10)
            monthString = 0 + "" + month;
        else
            monthString = month + "";

        if(prevMonth < 10)
            prevMonthString = 0 + "" + prevMonth;
        else
            prevMonthString = prevMonth + "";


        query = String.format("select * from yahoo.finance.historicaldata where symbol = \"%s\" and startDate = \"%s\" and endDate = \"%s\""
        , symbol, prevMonthsYear + "-" + prevMonthString + "-" + prevMonthsDate, year + "-" + monthString + "-" + date);
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("AppInfo", "Query : " + query);

        lineChart = (LineChart) findViewById(R.id.chart);

        aQuery.ajax(BASE_URL + query + URL_QUALIFIERS, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                downloadedJson = object;
                Log.d("AppInfo", downloadedJson);
                populateDataSets();

            }
        });


    }



    private void populateDataSets(){
        try {

            JSONObject jsonObject = new JSONObject(downloadedJson);

            if (jsonObject != null && jsonObject.length() != 0) {

                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));

                if(count != 0) {

                    JSONArray resultArray = jsonObject.getJSONObject("results")
                            .getJSONArray("quote");

                    for(int i = 0; i < count; i++){

                        JSONObject quoteObject = resultArray.getJSONObject(i);
                        bidPrice.add(quoteObject.getString("Close"));
                        dateForPlot.add(quoteObject.getString("Date"));

                    }

                }
            }

            plotGraph();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void plotGraph(){

        ArrayList<String> reversedDateForPlot = new ArrayList<>();
        for(int i = dateForPlot.size() - 1; i >= 0; i--){
            reversedDateForPlot.add(dateForPlot.get(i));
        }

        LineData lineData = new LineData(reversedDateForPlot, getYAxisPoints());
        lineChart.setData(lineData);
        lineChart.setDragEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDescription(R.string.chart_description + symbol);
        lineChart.setDescriptionColor(Color.rgb(155, 155, 0));
        lineChart.animateX(2000);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.argb(255, 214, 106, 23));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        YAxis left = lineChart.getAxisLeft();
        left.setTextSize(10f);
        left.setDrawZeroLine(false);
        left.setTextColor(Color.argb(255, 214, 106, 23));
        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(10f);
        legend.setTextColor(Color.argb(255, 214, 106, 23));

        lineChart.invalidate();

    }

    private List<ILineDataSet> getYAxisPoints(){

        List<ILineDataSet> dataSets = null;

        ArrayList<Entry> entrySet = new ArrayList<>();



        for(int i = 0; i < bidPrice.size(); i++) {

            Entry entry = new Entry(Float.parseFloat(String.format("%.2f",
                    (float) Math.round(Float.parseFloat(bidPrice.get(bidPrice.size() - i - 1)) * 100) / 100)), i);
            entrySet.add(entry);

        }





        LineDataSet lineDataSet = new LineDataSet(entrySet, symbol);
        lineDataSet.setColor(Color.rgb(0, 255, 0));
        lineDataSet.setValueTextColor(Color.argb(255, 214, 106, 23));
        lineDataSet.setValueTextSize(10f);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            lineDataSet.setDrawValues(false);

        dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        return dataSets;

    }


}
