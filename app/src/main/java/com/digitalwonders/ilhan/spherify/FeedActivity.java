package com.digitalwonders.ilhan.spherify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class FeedActivity extends AppCompatActivity {

    private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
    private static final String TOKENURL = "https://api.instagram.com/oauth/access_token";
    public static final String APIURL = "https://api.instagram.com/v1";
    public static String CALLBACKURL = "https://www.facebook.com/spherify";
    private String client_id = "5c1f023b1abf4ac4a04e377e4710ce5f";
    private String client_secret = "540519ed85f54ea896624cafe51b4cf7";
    private String authURLString = AUTHURL + "?client_id=" + client_id + "&redirect_uri=" + CALLBACKURL + "&response_type=code&display=touch&scope=likes+comments+relationships";
    private String tokenURLString = TOKENURL + "?client_id=" + client_id + "&client_secret=" + client_secret + "&redirect_uri=" + CALLBACKURL + "&grant_type=authorization_code";
    private String request_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        authURLString = AUTHURL + "?client_id=" + client_id + "&redirect_uri=" + CALLBACKURL + "&response_type=code&display=touch&scope=likes+comments+relationships";
        tokenURLString = TOKENURL + "?client_id=" + client_id + "&client_secret=" + client_secret + "&redirect_uri=" + CALLBACKURL + "&grant_type=authorization_code";
    }

    @Override
    public void onResume() {
        super.onResume();

        /*WebView webView = (WebView)findViewById(R.id.webview);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new AuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://instagram.com/spherify/");*/

        String urlString = "https://instagram.com/spherify/media/";
        try {
            final URL url = new URL(urlString);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream inputStream = url.openConnection().getInputStream();
                        String response = streamToString(inputStream);

                        JSONObject jsonObject = (JSONObject) new JSONTokener(response).nextValue();
                        JSONArray jsonArray = jsonObject.getJSONArray("items");

                        for (int i = 0; i < 10 && i < jsonArray.length(); i++) {
                            JSONObject mainImageJsonObject = jsonArray.getJSONObject(i).getJSONObject("images").getJSONObject("low_resolution");//Use for loop to traverse through the JsonArray.
                            String imageUrlString = mainImageJsonObject.getString("url");
                            Log.i("Spherify", imageUrlString);
                        }
                    } catch (IOException e) {
                        Log.e("Spherify", e.toString());
                    } catch (JSONException e) {
                        Log.e("Spherify", e.toString());
                    }
                }

            }).start();
        }
        catch (MalformedURLException e) {
            Log.e("Spherify", e.toString());
        }



    }

    public String streamToString(InputStream is) throws IOException {
        String string = "";

        if (is != null) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            string = stringBuilder.toString();
        }

        return string;
    }

    public class AuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(CALLBACKURL)) {
                System.out.println(url);
                String parts[] = url.split("=");
                request_token = parts[1];  //This is your request token.
                return true;
            }
            return false;
        }
    }
}
