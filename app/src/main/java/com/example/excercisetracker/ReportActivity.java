package com.example.excercisetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ReportActivity extends AppCompatActivity {
    String currentfilename;
    List<Location> userlocations;
    float totaldistance;
    Long totaltime;
    float averagespeed;
    float timeinseconds;
    Double minalt, maxalt;
    private TextView timeTaken, distanceCovered, maxAlt, minAlt, averageSpeed;
    private LinearLayout chartContainer;
    private LinearLayout mainView;
    private Button saveChart;
    List<Point> points = new ArrayList<>();
    private static final SimpleDateFormat gpxDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        currentfilename = getIntent().getStringExtra("filename");
        chartContainer = findViewById(R.id.chartContainer);
        timeTaken = findViewById(R.id.total_time);
        distanceCovered = this.findViewById(R.id.total_distance);
        averageSpeed = this.<TextView>findViewById(R.id.average_speed);
        maxAlt = this.<TextView>findViewById(R.id.max_altitude);
        minAlt = this.<TextView>findViewById(R.id.min_altitude);
        saveChart = findViewById(R.id.saveChart);
        saveChart.setOnClickListener(v -> {
            saveChartAsImage();
        });
        mainView = findViewById(R.id.mainView);
        toastMessage(currentfilename.toString());
        String path = Environment.getExternalStorageDirectory().toString() + "/GPStracks/" + currentfilename;
        File gpxFile = new File(path);
        try {
            userlocations = readGpxFile(gpxFile);
            this.runOnUiThread(() -> {
                if (userlocations.size() > 0) {
                    calculatevalues();
                    updateview();
                } else {
                    toastMessage("No data available in file");
                }
            });
        } catch (SAXParseException e) {
            e.printStackTrace();
        }
    }

    private void saveChartAsImage() {
        String root = Environment.getExternalStorageDirectory().toString();
        File file = new File(root + "/GPStracks/" + new Date().getTime() + ".jpg");
        Bitmap b = mainView.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(file.getPath()));
            toastMessage("Saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculatevalues() {
        totaldistance = 0;
        long baseTime = userlocations.get(0).getTime();
        for (int i = 0; i < userlocations.size() - 2; i++) {
            long distanceInTwoPoints = (long) userlocations.get(i).distanceTo(userlocations.get(i + 1));
            long time = (userlocations.get(i + 1).getTime() - baseTime) / 1000;
            Point p = new Point(time, distanceInTwoPoints);
            p.setxAxis(p.getxAxis());// seconds to Hour
            p.setyAxis(p.getyAxis());// meters to KM
            points.add(p);
            totaldistance = totaldistance + distanceInTwoPoints;
        }
        totaltime = userlocations.get(userlocations.size() - 1).getTime() - userlocations.get(0).getTime();
        timeinseconds = totaltime / 1000;
        averagespeed = totaldistance / timeinseconds;
        minalt = userlocations.get(0).getAltitude();
        maxalt = 0.00;
        for (int i = 0; i < userlocations.size() - 1; i++) {
            if (userlocations.get(i).getAltitude() < minalt) {
                minalt = userlocations.get(i).getAltitude();
            }
            if (userlocations.get(i).getAltitude() > maxalt) {
                maxalt = userlocations.get(i).getAltitude();
            }
        }
    }


    private void updateview() {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        timeTaken.setText(String.valueOf(timeinseconds) + " secs");
        distanceCovered.setText(String.valueOf(totaldistance) + " meters");
        averageSpeed.setText(String.valueOf(df.format(averagespeed)) + " m/s");
        minAlt.setText(df.format(minalt).toString());
        maxAlt.setText(df.format(maxalt).toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                buildChart();
            }
        });
    }

    private void buildChart() {
        chartContainer.removeAllViews();
        mainView.setDrawingCacheEnabled(true);
        ChartView chart = new ChartView(this, points);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(50, 50, 50, 50);
        chart.setLayoutParams(params);
        chartContainer.addView(chart);
    }

    private List<Location> readGpxFile(File file) throws SAXParseException {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        List<Location> list = new ArrayList<Location>();

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            FileInputStream fileInputStream = new FileInputStream(file);
            Document document = documentBuilder.parse(fileInputStream);
            Element elementRoot = document.getDocumentElement();
            NodeList nodelist_trkpt = elementRoot.getElementsByTagName("trkpt");
            for (int i = 0; i < nodelist_trkpt.getLength(); i++) {
                Node node = nodelist_trkpt.item(i);
                NamedNodeMap attributes = node.getAttributes();
                String newLatitude = attributes.getNamedItem("lat").getTextContent();
                Double newLatitude_double = Double.parseDouble(newLatitude);
                String newLongitude = attributes.getNamedItem("lon").getTextContent();
                Double newLongitude_double = Double.parseDouble(newLongitude);
                String newLocationName = newLatitude + ":" + newLongitude;
                Location newLocation = new Location(newLocationName);
                newLocation.setLatitude(newLatitude_double);
                newLocation.setLongitude(newLongitude_double);
                NodeList nList = node.getChildNodes();
                for (int j = 0; j < nList.getLength(); j++) {
                    Node el = nList.item(j);
                    if (el.getNodeName().equals("ele")) {
                        newLocation.setAltitude(Double.parseDouble(el.getTextContent()));
                    } else if (el.getNodeName().equals("time")) {
                        try {
                            Date d = sdf.parse(el.getTextContent());
                            newLocation.setTime(d.getTime());
                        } catch (ParseException ex) {
                            Log.v("Exception", ex.getLocalizedMessage());
                        }


                    }
                }

                list.add(newLocation);

            }

            fileInputStream.close();

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            toastMessage("Error in reading file");
        }

        return list;
    }


    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}