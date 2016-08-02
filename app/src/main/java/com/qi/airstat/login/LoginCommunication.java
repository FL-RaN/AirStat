package com.qi.airstat.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.qi.airstat.Constants;

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

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class LoginCommunication {
    /*
    Server Address
     */
    private static final String STR_URL = "http://";

    /*
    Widget Instance
     */
    private LoginUi loginUi;

    /*
    Inner class for communications
     */
    private LoginAsync loginAsync;

    /*
    LoginBaseActivity context for Toast
     */
    private Context loginActivityContext;

    private String email;
    private String password;

    /*
    Constructor
     */
    public LoginCommunication(Context loginActivityContext, LoginUi loginUi) {
        this.loginUi = loginUi;
        this.loginActivityContext = loginActivityContext;
        this.loginAsync = new LoginAsync();

        email = loginUi.edtEmail.getText().toString().trim();
        password = loginUi.edtPassword.getText().toString().trim();

        loginAsync.execute(email, password); //Communications using AsyncTask
    }

    /*
    Communications using AsyncTask
     */
    private class LoginAsync extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(loginActivityContext);
        HttpURLConnection conn;
        URL url = null;

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
                url = new URL(STR_URL);
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
                conn.setConnectTimeout(Constants.COMMUNICATION_CONNECT_TIMEOUT);
                conn.setReadTimeout(Constants.COMMUNICATION_READ_TIMEOUT);
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
                jsonQuery.put(Constants.COMMUNICATION_MSG_ID, Constants.COMMUNICATION_LOGIN_REQUEST);
                jsonQuery.put(Constants.COMMUNICATION_USER_EMAIL, strings[0]);
                jsonQuery.put(Constants.COMMUNICATION_USER_PASSWORD, strings[1]);
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
            } else if (result.equalsIgnoreCase("false")) {
                makeToast("Invalid email or password");
            } else if (result.equalsIgnoreCase("MalformedURLException") || result.equalsIgnoreCase("Exception") || result.equalsIgnoreCase("IOException") || result.equalsIgnoreCase("unsuccessful")) {
                makeToast("Something went wrong. Connection Problem");
            }
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(loginActivityContext, msg, Toast.LENGTH_LONG).show();
    }
}
