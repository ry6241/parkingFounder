package com.yadav.maps4;

/**
 * Created by Rohit yadav on 9/16/2016.
 */

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


/**
 * Created by Rohit yadav on 6/16/2016.
 */

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    static String charset="UTF-8";
    HttpURLConnection conn;
    DataOutputStream  wr;
    StringBuilder result;
    URL urlObj;
    JSONObject jobj = null;
    String paramString;
    // constructor
    public JSONParser() {

    }

    public JSONObject getJSONFromUrl(String url, String method, HashMap<String,String> params) {

        StringBuilder sbParams=new StringBuilder();
        int i=0;
        for(String key : params.keySet()){
            try {
                if (i != 0) {
                    sbParams.append("&");
                }
                sbParams.append(key).append("=").append(URLEncoder.encode(params.get(key), charset));
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            i++;
        }

        if(method.equals("POST")){
            try{
                urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset",charset);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.connect();
                paramString = sbParams.toString();
                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramString);
                wr.flush();
                wr.close();

            }catch (IOException e){
                e.printStackTrace();
            }
        }
        else if(method.equals("GET")){

            if(sbParams.length() != 0){
                url += "?" + sbParams.toString();
            }

            try{
                urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset",charset);
                conn.setConnectTimeout(15000);
                conn.connect();

            }catch (IOException e){
                e.printStackTrace();
            }
        }


      //Recieve resonse
        try {

            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                result.append(line);
            }
            Log.d("JSONParser","result: "+ result.toString());
        }catch (IOException e){
            e.printStackTrace();
        }

        conn.disconnect();

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }

}
