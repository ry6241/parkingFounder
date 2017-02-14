package com.yadav.maps4;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rohit yadav on 9/16/2016.
 */
public class ServerConnection {

    private JSONParser jsonParser;

    //URL of the PHP API
    private static String parkingLocationsURL = "http://192.168.4.41:8080/parkit_server/parkingData";

    private static String login_tag = "login";;


    // constructor
    public ServerConnection(){
        jsonParser = new JSONParser();
    }

    /**
     * Function to Login
     **/

    public JSONObject getNearByParkings(String latitude, String longitude){
        // Building Parameters
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("latitude",latitude);
        params.put("longitude",longitude);

        //RR
      //  JSONObject json = jsonParser.getJSONFromUrl(parkingLocationsURL,"POST", params);
      /*  {"success":
                 "1","locations":
                            {"1":
                                {"fixed_rate":40,"second_hr_rate":0,"latitude":28.6115048,"beyond_second_hr":0,"ID":1,"longitude":77.0351793,"first_hr_rate":0},
                                "2":{"fixed_rate":0,"second_hr_rate":30,"latitude":28.6196145,"beyond_second_hr":50,"ID":2,"longitude":77.0265738,"first_hr_rate":20},
                                "3":{"fixed_rate":50,"second_hr_rate":0,"latitude":28.7640044,"beyond_second_hr":0,"ID":3,"longitude":76.0564593,"first_hr_rate":0}}} */
        JSONObject json= new JSONObject();
        JSONObject locations = new JSONObject();

        JSONObject parking_loc = new JSONObject();

        try {
            parking_loc.put("ID",1);
        parking_loc.put("latitude", Double.valueOf(latitude) + 0.01);
        parking_loc.put("longitude",Double.valueOf(longitude) + 0.01);
        parking_loc.put("fixed_rate", 40);
        parking_loc.put("first_hr_rate", 0);
        parking_loc.put("second_hr_rate", 0);
        parking_loc.put("beyond_second_hr", 0);
            parking_loc.put("name","MCD Parking");
            parking_loc.put("store_add", "Shiv Vihar, Sec 3 Dwarka New delhi");
        locations.put(Integer.toString(1), parking_loc);

            json.put("success","1");
            json.put("locations", locations);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("json",json.toString());
        return json;
    }

}
