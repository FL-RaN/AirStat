package com.qi.airstat;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by JUMPSNACK on 8/2/2016.
 */
public class HttpService extends AsyncTask<String, String, String> {

    private Context context;
    private ArrayList<String> params;
    ProgressDialog pdLoading;
    HttpURLConnection conn;
    String strUrl;
    URL url;

    String receivedData;

    public String executeConn(Context context, String strUrl, ArrayList<String> params) {
        this.context = context;
        this.params = params;

        if (context != null)
            this.pdLoading = new ProgressDialog(context);

        this.strUrl = strUrl;
        try {
            Log.w("HTTPSERVICE", "FIRST");
            receivedData = this.execute(strUrl).get(600, TimeUnit.MILLISECONDS);
            return receivedData;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context != null) {
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }
        Log.w("HTTPSERVICE", "SECOND");
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            url = new URL(strings[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "MalformedURLException";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
        String sendingResult = sendToServer(strings);
        if (sendingResult != null) return sendingResult;

        String receivingResult = receiveFromServer();
        if (receivingResult != null) return receivingResult;

        return null;
    }


    @Nullable
    private String sendToServer(String[] strings) {
        try {
                /*
                Set HttpURLConnection to send and receive data from php and mysql
                 */
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
            conn.setReadTimeout(Constants.HTTP_READ_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            Log.w("URL", strUrl);

                /*
                SetDoInput and setDoOutput method depict handling of both send and receive
                 */
            conn.setDoInput(true);
            conn.setDoOutput(true);

                /*
                JSON - Append parameters to JSON
                 */
            JSONObject jsonQuery = new JSONObject();
            for (int i = 0; i < params.size(); i++) {
                jsonQuery.put(params.get(i), params.get(++i));
            }
            String query = jsonQuery.toString();

            /*!!!!! JUST FOR TEST !!!!!*/
            if (strUrl.equals(Constants.HTTP_STR_URL_TEST))
                query = "{\n" +
                        "   \"HR\":[{\"timeStamp\":\"190408031123\",\n" +
                        "    \"connectionID\":1,\n" +
                        "    \"heartrate\":80,\n" +
                        "    \"latitude\":33.88,\n" +
                        "    \"longitude\":-101.11\n" +
                        "    }],\n" +
                        "    \"AIR\":[{\n" +
                        "    \"timeStamp\":\"190805141222\",\n" +
                        "    \"connectionID\":1,\n" +
                        "    \"SO2\":23.3,\n" +
                        "    \"NO2\":13.1,\n" +
                        "    \"O3\":22.1,\n" +
                        "    \"CO\":40.1,\n" +
                        "    \"PM\":7.3,\n" +
                        "    \"temperature\":38.6,\n" +
                        "    \"latitude\":33.88,\n" +
                        "    \"longitude\":-101.11\n" +
                        "    }]\n" +
                        "}";


            Log.w("SND", query);

                /*
                Open connection for sending data
                 */
            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            outputStream.close();
            conn.connect();


        } catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        }
        return null;
    }

    @NonNull
    private String receiveFromServer() {
        try {
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                String strResult = result.toString();

                /* Processing for PROTOCOL */
//                switch (strResult.charAt(0)) {
//                    case '1':
////                        receivedData = strResult;
//                        return strResult;
//                    case '2':
//                        return "false";
//                    default:
////                        receivedData = strResult;
//                        return strResult; //Temporary setting
//                }

                Log.w("HTTPSERVICE", "THIRD");
                return strResult;
            } else {
                Log.w("RSP CODE", String.valueOf(responseCode));
                return "unsuccessful";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception";
        } finally {
            conn.disconnect();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (context != null)
            pdLoading.dismiss();

        /* Err CASE*/
        if (result.equalsIgnoreCase("MalformedURLException") || result.equalsIgnoreCase("Exception") || result.equalsIgnoreCase("IOException") || result.equalsIgnoreCase("unsuccessful")) {
            makeToast("Something went wrong. Connection Problem");
        } else { /* Success CASE */
        }
    }

    private void makeToast(String msg) {
        if (context != null)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}

