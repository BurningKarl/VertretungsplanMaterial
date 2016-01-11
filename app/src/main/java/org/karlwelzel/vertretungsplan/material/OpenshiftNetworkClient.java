package org.karlwelzel.vertretungsplan.material;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Karl on 03.10.2015.
 */


public class OpenshiftNetworkClient { //Recommended here: http://loopj.com/android-async-http/#recommended-usage-make-a-static-http-client
    private static final String BASE_URL = "http://cjd-vplan.rhcloud.com/";
    private static final Header[] BASE_HEADERS = {};
    private static AsyncHttpClient client = new AsyncHttpClient();


    public static void get(Context context, String url, JsonHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), new Header[]{}, new RequestParams(), responseHandler);
    }

    public static void get(Context context, String url, Header[] headers, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(context, getAbsoluteUrl(url), headers, params, responseHandler);
    }

    public static void getSubstituteSchedule(Context context, JsonHttpResponseHandler responseHandler) {
        get(context, "VertretungsplanJSON", responseHandler);
    }

    public static void getSubjectSelectionData(Context context, JsonHttpResponseHandler responseHandler) {
        get(context, "BelegungJSON", responseHandler);
    }

/*
    public static void post(Context context, String url, Header[] headers, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.post(context, getAbsoluteUrl(url), getHeaders(headers), params, "application/json", responseHandler);
    }
*/

    public static String getAbsoluteUrl(String url) {
        return BASE_URL + url;
    }
}
