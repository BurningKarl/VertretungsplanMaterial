package org.karlwelzel.vertretungsplan.material;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;

/**
 * Created by Karl on 03.10.2015.
 */


public class ParseRestClient { //Recommended here: http://loopj.com/android-async-http/#recommended-usage-make-a-static-http-client
    private static final String BASE_URL = "https://api.parse.com:443/1/";
    private static final Header[] BASE_HEADERS = {
            new BasicHeader("X-Parse-Application-Id", "asbwHDQkkpWte63B7XQptlAginCXi5vPbfF41MpB"),
            new BasicHeader("X-Parse-REST-API-Key", "iQ1nEs9y2rHbNh3MfV1gZGmks5vy5JLNnlfWYgXj"),
            new BasicHeader("Content-Type", "application/json")
    };
    private static AsyncHttpClient client = new AsyncHttpClient();


    public static void get(Context context, String url, JsonHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), getHeaders(), new RequestParams(), responseHandler);
    }

    public static void get(Context context, String url, Header[] headers, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), getHeaders(headers), params, responseHandler);
    }

    public static void getSubstituteSchedule(Context context, JsonHttpResponseHandler responseHandler) {
        get(context, "classes/VertretungsplanJSON/dtTtqm4qef", responseHandler);
    }

    public static void getSubjectSelectionData(Context context, JsonHttpResponseHandler responseHandler) {
        get(context, "classes/BelegungJSON/PN2nsg9FI3", responseHandler);
    }

    public static void post(Context context, String url, Header[] headers, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), getHeaders(headers), params, "application/json", responseHandler);
    }

    public static String getAbsoluteUrl(String url) {
        return BASE_URL + url;
    }

    public static Header[] getHeaders() {
        return BASE_HEADERS;
    }

    public static Header[] getHeaders(Header[] headers) { //join BASE_HEADERS and headers
        Header[] result = Arrays.copyOf(BASE_HEADERS, BASE_HEADERS.length + headers.length);
        System.arraycopy(headers, 0, result, BASE_HEADERS.length, headers.length);
        return result;
    }
}
