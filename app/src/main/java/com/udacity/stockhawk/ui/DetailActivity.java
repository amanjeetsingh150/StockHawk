package com.udacity.stockhawk.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.github.mikephil.charting.charts.LineChart;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.udacity.stockhawk.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import butterknife.BindView;

public class DetailActivity extends AppCompatActivity {

    TextView range;
    Toolbar toolbar;
    private int minClose,maxClose;
    private long minDate,maxDate;
    LineChartView lineChartView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        String title=getIntent().getStringExtra("symbol");
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        range=(TextView)findViewById(R.id.range);
        lineChartView= (LineChartView) findViewById(R.id.linechart);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new GraphData().execute(title);
    }
    private class GraphData extends AsyncTask<String,Void,LineSet>{

        @Override
        protected LineSet doInBackground(String... strings) {
            LineSet line=new LineSet();
            OkHttpClient okHttpClient=new OkHttpClient();
            Request request=new Request.Builder().
                    url("http://chartapi.finance.yahoo.com/instrument/1.0/" + strings[0] + "/chartdata;type=quote;range=6m/json")
                    .build();
            Response response;
            try{
                response=okHttpClient.newCall(request).execute();
                String responseJson=response.body().string();
                String jsonString=responseJson.split("finance_charts_json_callback\\(")[1].split("\\)")[0];
                JSONObject jsonObject;
                try{
                    jsonObject=new JSONObject(jsonString);
                    JSONArray array=jsonObject.getJSONArray("series");
                    if(array.length()>0){
                        minClose=array.getJSONObject(0).getInt("close");
                        maxClose=minClose;
                        minDate=array.getJSONObject(0).getLong("Date");
                        maxDate=minDate;
                    }
                    for(int i=0;i<array.length();i++){
                        int close=array.getJSONObject(i).getInt("close");
                        long date=array.getJSONObject(i).getInt("Date");
                        line.addPoint("",close);
                        if(close>maxClose){
                            maxClose=close;
                        }
                        if(close<minClose){
                            minClose=close;
                        }
                        if(date>maxDate){
                            maxDate=date;
                        }
                        if(date<minDate){
                            minDate=date;
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return line;
        }

        @Override
        protected void onPostExecute(LineSet lineSet) {
            super.onPostExecute(lineSet);
            Paint paint=new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(Tools.fromDpToPx(1f));
            lineChartView.setBorderSpacing(1)
                    .setAxisBorderValues(minClose, maxClose)
                    .setXLabels(AxisRenderer.LabelPosition.OUTSIDE)
                    .setYLabels(AxisRenderer.LabelPosition.OUTSIDE)
                    .setLabelsColor(Color.BLACK)
                    .setXAxis(false)
                    .setYAxis(false)
                    .setBorderSpacing(Tools.fromDpToPx(5));
            String sRange = DetailActivity.formatDate(String.valueOf(minDate)) + " - " + DetailActivity.formatDate(String.valueOf(maxDate));
            range.setText(sRange);
            if(lineSet.size()>0){
                lineChartView.addData(lineSet);
                lineChartView.show();
            }
        }
    }
    public static String formatDate(String time) {
        String date = "";
        DateFormat format = new SimpleDateFormat("dd MMM,yy");
        try {
            date = format.format(new SimpleDateFormat("yyyyMMdd").parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
