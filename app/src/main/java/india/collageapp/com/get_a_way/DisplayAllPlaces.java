package india.collageapp.com.get_a_way;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
public class DisplayAllPlaces extends AppCompatActivity
{
    // String listName="Hello World\n";
    String suggestion_name = "";
    String suggestion_latlng ="";
    //int total = 2;
    //int i=0;

    String waypoints_ ;
    String places_list ;

    String final_waypoints = "waypoints=optimize:true";
    String final_place_list = "";

    int no_of_places = 0 ;
    int places_counter = 0;
    int completed_flag = 0;

    private Hashtable<String, String> places_dict = new Hashtable<String, String>();
    private Hashtable<String, String> selected_places_dict = new Hashtable<String, String>();


    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displayallplaces);
        Log.d("RESULT", "hello");
        //   TextView t = (TextView)findViewById(R.id.suggest);

        Bundle bundle = getIntent().getExtras();
        //   selectedPlaces_id = (ArrayList<String>)bundle.getSerializable("selectedPlaces_id");

        // waypoints contains lat and lng
        waypoints_ = (String)bundle.getSerializable("waypoints");
        // places list contains place names
        places_list = (String)bundle.getSerializable("places_list");



        Log.e("waypoints: " , waypoints_);
        Log.e("places list : ", places_list);

        String[] data = waypoints_.split("\\|");
        String[] place_names = places_list.split("\\|");

        no_of_places = data.length - 1;
        //Log.e("list : " , Arrays.toString(data));

        if(data.length > 1)
        {
            for (int i = 1; i < data.length; ++i) {
                String[] lat_lng = data[i].split(",");

                LatLng loc1 = new LatLng(Double.parseDouble(lat_lng[0]), Double.parseDouble(lat_lng[1]));

                //addMarkers(mMap,tmp,place_names[i]);

                places_dict.put(place_names[i], data[i]);
            }
        }

        add_cards();

        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PathGoogleMapActivity.class);
                //     intent.putExtra("selectedPlaces_id", selectedPlaces);

                // TODO : update waypoints here

                for (String key : places_dict.keySet()) {

                    // System.out.println("key: " + key + " value: " + places_dict.get(key));
                    // waypoints = waypoints + "|" + place.getLatLng().latitude + "," + place.getLatLng().longitude ;
                    final_waypoints = final_waypoints + "|" + places_dict.get(key) ;
                    final_place_list = final_place_list + "|" + key ;
                }
                intent.putExtra("waypoints",final_waypoints);
                intent.putExtra("places_list",final_place_list);

                Log.e("Final Places @: " , final_place_list );
                Log.e("Final Latlng @: " , final_waypoints);
                startActivity(intent);
            }
        });

        Log.e("Final places @ : " , suggestion_name);
        Log.e("Final latlng  @ : " , suggestion_latlng);
    }

    void add_cards()
    {
        container = (LinearLayout)findViewById(R.id.container);

        String[] sugg_names = places_list.split("\\|");
        String[] sugg_latlng = waypoints_.split("\\|");

        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int i = 1 ; i< sugg_names.length ; ++i) {


            final View addView = layoutInflater.inflate(R.layout.row, null);
            final TextView textOut = (TextView) addView.findViewById(R.id.textout);
            textOut.setText(sugg_names[i]);



            Button buttonRemove = (Button) addView.findViewById(R.id.remove);
            buttonRemove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((LinearLayout) addView.getParent()).removeView(addView);
                    Log.e("Removed : ", textOut.getText() + "");

                    places_dict.remove(textOut.getText() + "");
                    Log.e("place dict", places_dict.toString());

                    /*waypoints_ = waypoints_ + "|" + places_dict.get(place_name) ;
                    places_list = places_list + "|" + place_name ;
                    */

                }
            });

            container.addView(addView);

        }
    }


}
