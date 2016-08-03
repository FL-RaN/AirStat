package com.qi.airstat;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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

/**
 * Created by JUMPSNACK on 8/2/2016.
 */
public class HttpService extends AsyncTask<String, String, String> {

    private Context context;
    private ArrayList<String> params;
    ProgressDialog pdLoading;
    HttpURLConnection conn;
    URL url;

    DialogFragment dialogFragment;
    FragmentManager fragmentManager;

    public void excuteConn(Context context, ArrayList<String> params, DialogFragment dialogFragment, FragmentManager fragmentManager) {
        this.context = context;
        this.params = params;
        this.dialogFragment = dialogFragment;
        this.fragmentManager = fragmentManager;

        this.pdLoading = new ProgressDialog(context);

        this.execute();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pdLoading.setMessage("\tLoading...");
        pdLoading.setCancelable(false);
        pdLoading.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            url = new URL(Constants.HTTP_STR_URL);
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

                /*
                SetDoInput and setDoOutput method depict handling of both send and receive
                 */
            conn.setDoInput(true);
            conn.setDoOutput(true);

                /*
                CASE#1. Query String - Append parameters to URL
                 */
//                Uri.Builder builder = new Uri.Builder()
//                        .appendQueryParameter(Constants.COMMUNICATION_USER_EMAIL, strings[0])
//                        .appendQueryParameter(Constants.COMMUNICATION_USER_PASSWORD, strings[1]);
//                String query = builder.build().getEncodedQuery();


                /*
                CASE#2. JSON - Append parameters to JSON
                 */
            JSONObject jsonQuery = new JSONObject();
            for (int i = 0; i < params.size(); i++) {
                jsonQuery.put(params.get(i), params.get(++i));
            }
            String query = jsonQuery.toString();

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

                return result.toString();
            } else {
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
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        pdLoading.dismiss();
        if (result.equalsIgnoreCase("true")) {
            makeToast("Successfully connected!");
            if (dialogFragment != null)
                dialogFragment.show(fragmentManager, "");
        } else if (result.equalsIgnoreCase("false")) {
            makeToast("Invalid information");
        } else if (result.equalsIgnoreCase("MalformedURLException") || result.equalsIgnoreCase("Exception") || result.equalsIgnoreCase("IOException") || result.equalsIgnoreCase("unsuccessful")) {
            makeToast("Something went wrong. Connection Problem");
        }
//        if (dialogFragment != null)
//            dialogFragment.show(fragmentManager, "");
    }

    private void makeToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}

