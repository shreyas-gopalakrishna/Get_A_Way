package india.collageapp.com.get_a_way;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.Manifest;

public class PathGoogleMapActivity extends FragmentActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_API_CLIENT_ID = 0;


    private static final String LOG_TAG = "PathGoogleMapActivity";
    GoogleMap mMap;
    private ArrayList<String> selectedPlaces_id = new ArrayList<String>() ;
    private ArrayList<Place> selectedPlaces = new ArrayList<Place>() ;
    final String TAG = "PathGoogleMapActivity";
    double current_latitude;
    double current_longitude ;
    String way_order = "";
    String waypoints_ ;
    String places_list ;
    private int got_places_flag = 0;
    private static final int PERMISSION_REQUEST_CODE = 1;
    BitmapDescriptor[] icon ;
    int dest;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent service = new Intent(getBaseContext(), ChatHeadService.class);
        startService(service);
        icon = new BitmapDescriptor[15];
        setContentView(R.layout.route);
        setUpMapIfNeeded();
        ReadTask downloadTask = new ReadTask();

        mGoogleApiClient = new GoogleApiClient.Builder(PathGoogleMapActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();


        Bundle bundle = getIntent().getExtras();
     //   selectedPlaces_id = (ArrayList<String>)bundle.getSerializable("selectedPlaces_id");
        waypoints_ = (String)bundle.getSerializable("waypoints");
        places_list = (String)bundle.getSerializable("places_list");

        Log.e("waypoints: ", waypoints_);
        Log.e("places : ", places_list );


       // set_up_markers(waypoints_ , places_list);
/*
         Log.e("sel places : ", selectedPlaces_id.toString());
        if(selectedPlaces_id != null)
        {
            for (String s : selectedPlaces_id) {
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, s);
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);


                Log.e("Called getPlaceById for" , s);

            }
        }

*/

        GPSTracker gps = new GPSTracker(this);
        if(gps.canGetLocation())
        {
            current_latitude = gps.getLatitude(); // returns latitude
            current_longitude = gps.getLongitude(); // returns longitude
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + current_latitude + "\nLong: " + current_longitude, Toast.LENGTH_LONG).show();

        }
        else
        {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        //LatLng curr_loc = new LatLng(current_latitude,current_longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current_latitude, current_longitude), 12.0f));


        String url = getMapsApiDirectionsUrl();
        downloadTask.execute(url);




    }
/*
    private void set_up_markers(String places , String places_list)
    {
        String[] data = places.split("\\|");
        String[] place_names = places_list.split("\\|");
        //Log.e("list : " , Arrays.toString(data));
        if(data.length > 1) {
            for (int i = 1; i < data.length; ++i)
            {
                String[] lat_lng = data[i].split(",");

                LatLng tmp = new LatLng(Double.parseDouble(lat_lng[0]),Double.parseDouble(lat_lng[1]) );

                addMarkers(mMap,tmp,place_names[i]);

            }
        }

    } */

    private void set_up_markers(String places , String places_list , String order )
    {
        String[] data = places.split("\\|");
        String[] place_names = places_list.split("\\|");
        String[] x = order.split("\\[");
        String[] y = x[1].split("\\]");

        String[] w_o = y[0].split(",");

        Log.e("list : " , Arrays.toString(data));
        Log.e("w_o : " , Arrays.toString(w_o));

        dest = w_o.length;
        if(w_o.length > 0) {
            for (int i = 0; i < w_o.length; ++i)
            {
                int j = Integer.parseInt(w_o[i]) + 1;

                String[] lat_lng = data[j].split(",");

                Log.e(" check : " , i + "  :  " +place_names[j]+"");
                LatLng tmp = new LatLng(Double.parseDouble(lat_lng[0]),Double.parseDouble(lat_lng[1]) );


                addMarkers(mMap,tmp,place_names[j],i + 1);

            }
        }

    }


    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());

                places.release();

                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            //CharSequence attributions = places.getAttributions();

       //     selectedPlaces.add(place);
            Log.e("place name : ", place.getAddress() + "");

           // addMarkers(mMap,place);

            places.release();
         //   Log.e("sel len : ", selectedPlaces.size() + "");

            got_places_flag =1;

        }

            //selectedPlaces_id.add(place.getId()+"");



    };

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.route_map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                icon[1] = BitmapDescriptorFactory.fromResource(R.drawable.number_1);
                icon[2] = BitmapDescriptorFactory.fromResource(R.drawable.number_2);
                icon[3] = BitmapDescriptorFactory.fromResource(R.drawable.number_3);
                icon[4] = BitmapDescriptorFactory.fromResource(R.drawable.number_4);
                icon[5] = BitmapDescriptorFactory.fromResource(R.drawable.number_5);
                icon[6] = BitmapDescriptorFactory.fromResource(R.drawable.number_6);
                icon[7] = BitmapDescriptorFactory.fromResource(R.drawable.number_7);
                icon[8] = BitmapDescriptorFactory.fromResource(R.drawable.number_8);
                icon[9] = BitmapDescriptorFactory.fromResource(R.drawable.number_9);
                icon[10] = BitmapDescriptorFactory.fromResource(R.drawable.number_10);
                icon[11] = BitmapDescriptorFactory.fromResource(R.drawable.number_11);
                icon[12] = BitmapDescriptorFactory.fromResource(R.drawable.number_12);
                icon[13] = BitmapDescriptorFactory.fromResource(R.drawable.number_13);
                icon[14] = BitmapDescriptorFactory.fromResource(R.drawable.number_14);
            }
        }
    }

    private void setUpMap() {
        /*
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

   //         ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION);
            // namesake check for permission works without this (used to simply remove syntax errors)
        }*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
/*            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    2);*/

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

                Toast.makeText(this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }

        mMap.setMyLocationEnabled(true);

    }

    private String getMapsApiDirectionsUrl() {


        /*
        String waypoints = "waypoints=optimize:true|"
                + LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude
                + "|" + BROOKLYN_BRIDGE.latitude + "," + BROOKLYN_BRIDGE.longitude
                + "|" + WALL_STREET.latitude + "," + WALL_STREET.longitude;
*/
        double dst_lat = current_latitude + 0.00001;
        double dst_long = current_longitude + 0.00001;

        String sensor = "sensor=false";
       String origin ="origin=" + current_latitude + "," + current_longitude;

        String destination = "destination=" + dst_lat + "," + dst_long;

        String waypoints = waypoints_;

        String params = origin + "&" + destination + "&" + waypoints + "&" + sensor + "&key=AIzaSyCmFHV-bPjsVKSe0xaRPwwFSVBCmQGJPLo";


        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        Log.e("url : ", url);
        return url;
    }

    private void addMarkers(GoogleMap mMap, Place place) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                    .title(place.getName()+""));

        }
    }

    //  add the marker no. j at the given latlng , (j ranges from 1 to no. of waypoints as 0 is souce/current locn )
    private void addMarkers(GoogleMap mMap, LatLng latlng , String name , int j ) {

        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(latlng)
                    .title(name).icon(icon[j]));

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "Google Places API connected.");

    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    private ProgressDialog mDialog;

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = ProgressDialog.show(PathGoogleMapActivity.this, "Please wait...", "Drawing Route ...", true);

        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("exc doInBackground", e.toString());

            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            String distance_ = "";
            String duration = "";


            double distance =0.0,ld=0.0;
            int hr=0,lhr=0,min=0,lmin=0;
            // traversing through routes
            Log.e("routes len : " , routes.size()+"");
            if(routes!=null)
            {
                String[] dist;
                String[] time;

                Log.d("order : " , routes.toString());

                for (int i = 0; i < routes.size(); i++) {
                //for (int i = 0; i < 1; i++) {
                    points = new ArrayList<LatLng>();
                    polyLineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = routes.get(i);

                    Log.e("Path len : " , path.size() + "");

                    //Log.e("order : " , path.get("waypoint_order"));



                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                    /*    if(j==0){    // Get distance from the list
                            distance = (String)point.get("distance") + "";
                            Log.e("Dist recd : " , distance);
                            continue;
                        }
                        else if(j==1){ // Get duration from the list
                            duration = (String)point.get("duration") + "";
                            Log.e("Dist recd : " , distance);
                            continue;
                        }
*/
                        Set<String> keys = point.keySet();

                        for (String x : keys)
                        {

                            String value = (String) point.get(x);
                            if(x.equals("distance"))
                            {
                                Log.e("Dist recd : " , value);
                                dist = value.split(" ");
                                Log.e("Dist recd : " , String.valueOf(dist.length));
                                if(dist[1].equals("km"))
                                {
                                    ld = Double.parseDouble(dist[0]);
                                    distance += ld;
                                }
                            }
                            else if (x.equals("duration"))
                            {
                                Log.e("Dur recd : " , value);
                                time = value.split(" ");
                                if(time[1].equals("hours"))
                                {
                                    lhr = Integer.parseInt(time[0]);
                                    hr += lhr;
                                    if(time.length > 2)
                                    {
                                        lmin = Integer.parseInt(time[2]);
                                        min += lmin;
                                    }
                                }
                                else
                                {
                                    lmin = Integer.parseInt(time[0]);
                                    min += lmin;
                                }
                                Log.e("Dur recd : ", value);

                            }

                            else if (x.equals("waypoint_order"))
                            {
                                way_order = value;
                                Log.e("way_order : " , way_order);
                            }
                            else
                            {
                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);

                                points.add(position);
                            }

                        }


                    }

                    polyLineOptions.addAll(points);
                    polyLineOptions.width(10);
                    polyLineOptions.color(Color.BLUE);
                }
                mDialog.dismiss();

                distance -= ld; //subtracting distance back to source
                hr -= lhr;
                min -= lmin;
                if(min > 60)
                {
                    hr += (min/60);
                    min = min %60;
                }
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);

                String h = (hr > 0)? String.valueOf(hr)+" hours " : "";
                String m = (min > 0)? String.valueOf(min)+" mins" : "";
                AlertDialog.Builder builder = new AlertDialog.Builder(PathGoogleMapActivity.this);
                builder.setTitle("Travel Summary")
                        .setMessage("Travel Distance: " + df.format(distance) + " km\n\nTravel Time: " + h + m + "\n")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                AlertDialog alert2 = builder.create();
                alert2.show();
            }

 //           googleMap.addPolyline(polyLineOptions);
            if(polyLineOptions!=null)
                mMap.addPolyline(polyLineOptions);

            set_up_markers(waypoints_ , places_list , way_order);        }


    }
}