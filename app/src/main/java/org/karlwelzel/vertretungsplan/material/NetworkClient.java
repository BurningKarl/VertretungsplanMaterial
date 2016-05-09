package org.karlwelzel.vertretungsplan.material;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Karl on 03.10.2015.
 */


public class NetworkClient { //Recommended here: http://loopj.com/android-async-http/#recommended-usage-make-a-static-http-client
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private static final String BASE_URL = "http://cjd-vplan.rhcloud.com/";
    private AsyncHttpClient client;

    public NetworkClient(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        client = new AsyncHttpClient();
        client.setTimeout(60000); //TODO: Remove later
    }

    public void setLogin(String username, String password) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public void get(String url, JsonHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), new Header[]{}, new RequestParams(), responseHandler);
    }

    public void get(String url, Header[] headers, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
    }

    public void post(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), params, responseHandler);
    }

    public void post(String url, JsonHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        Log.d("NetworkClient.post", "username: "+preferences.getString(KEY_USERNAME, ""));
        Log.d("NetworkClient.post", "password: "+preferences.getString(KEY_PASSWORD, ""));
        params.put("username", preferences.getString(KEY_USERNAME, ""));
        params.put("password", preferences.getString(KEY_PASSWORD, ""));
        post(url, params, responseHandler);
    }

    public void getSubstituteSchedule(JsonHttpResponseHandler responseHandler) {
        post("VertretungsplanJSON", responseHandler);
        //post("test", responseHandler);
    }

    public void getSubjectSelectionData(JsonHttpResponseHandler responseHandler) {
        get("BelegungJSON", responseHandler);
    }

    public static String getAbsoluteUrl(String url) {
        return BASE_URL + url;
    }
}
