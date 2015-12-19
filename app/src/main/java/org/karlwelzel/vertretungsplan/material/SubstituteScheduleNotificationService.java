package org.karlwelzel.vertretungsplan.material;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import cz.msebera.android.httpclient.Header;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */

public class SubstituteScheduleNotificationService extends IntentService {
    public SubstituteScheduleNotificationService() {
        super("SchedulingService");
    }

    public static final String TAG = "NotificationService";

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    public static final String BROADCAST_ACTION = "org.karlwelzel.vertretung-splantest.BROADCAST_ACTION";
    public static final String BROADCAST_DATA_STATUS = "org.karlwelzel.vertretungsplantest.BROADCAST_DATA_STATUS";

    //TODO: Give user the option to change this time (add to settings)
    public static final int[] timeShowAllEntriesFromToday = {6, 00};
    //Once per day after this time, all entries are displayed

    public static Looper looper = null;

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SubstituteScheduleNotificationService(String name) {
        super(name);
    }

    public static void doYourJob(Context context_) {
        final Context context = context_;

        Calendar calendarShowAllEntriesFromToday = Calendar.getInstance();
        calendarShowAllEntriesFromToday.setTimeInMillis(System.currentTimeMillis());
        calendarShowAllEntriesFromToday.set(Calendar.MILLISECOND, 0);
        calendarShowAllEntriesFromToday.set(Calendar.SECOND, 0);
        calendarShowAllEntriesFromToday.set(Calendar.MINUTE, timeShowAllEntriesFromToday[1]);
        calendarShowAllEntriesFromToday.set(Calendar.HOUR_OF_DAY, timeShowAllEntriesFromToday[0]);
        Date dateShowAllEntriesFromToday = calendarShowAllEntriesFromToday.getTime();

        Calendar calendarToday = Calendar.getInstance();
        calendarToday.setTimeInMillis(System.currentTimeMillis());
        calendarToday.set(Calendar.MILLISECOND, 0);
        calendarToday.set(Calendar.SECOND, 0);
        calendarToday.set(Calendar.MINUTE, 0);
        calendarToday.set(Calendar.HOUR_OF_DAY, 0);
        final Date dateToday = calendarToday.getTime();

        if (dateShowAllEntriesFromToday.after(SubstituteSchedule.getLastModifiedDate(context.getExternalFilesDir(null))) && dateShowAllEntriesFromToday.before(new Date())) {
            Log.d(TAG, "showAllEntriesFromToday");
            //show all entries from today
            JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "onSuccess");
                    try {
                        showAllEntries(new SubstituteSchedule(response.getString("json")));
                    } catch (JSONException | ParseException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (looper != null) looper.quit();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                    try {
                        showAllEntries(SubstituteSchedule.loadFromFile(context.getExternalFilesDir(null)));
                    } catch (JSONException | ParseException | IOException | ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } finally {
                        if (looper != null) looper.quit();
                    }
                }

                public void showAllEntries(SubstituteSchedule substituteSchedule) throws JSONException, IOException {
                    SubstituteScheduleDay today = substituteSchedule.getDay(dateToday);;

                    //get SubjectSelection of first subject selection
                    File subjectSelectionDir = context.getExternalFilesDir(SubjectSelection.SUBJECT_SELECTION_DIR_NAME);
                    SubjectSelection selection = SubjectSelection.loadFromFile(subjectSelectionDir,
                            SubjectSelection.subjectSelectionNames(subjectSelectionDir).get(0));

                    ArrayList<String> entries = today.getFilteredSubstituteScheduleEntries(selection);

                    String title, msg = "";
                    if (entries.size() > 0) {
                        for (int i = 0; i < entries.size(); i++) {
                            msg += entries.get(i) + "\n";
                        }
                        title = String.format("%1$ss Einträge von heute", selection.name);
                    } else {
                        title = String.format("%1$s hat heute keine Einträge", selection.name);
                    }

                    sendNotification(context, title, String.format("%1$d Einträge", entries.size()), msg);
                }
            };
            ParseRestClient.getSubstituteSchedule(context, responseHandler);

        } else if (isNetworkAvailable(context)) {
            //show only new entries
            Log.d(TAG, "showOnlyNewEntriesFromToday");

            JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d(TAG, "onSuccess");
                    try {
                        SubstituteSchedule substituteScheduleFromInternet = new SubstituteSchedule(response.getString("json"));
                        Log.d(TAG, dateToday.getTime() + " : " + substituteScheduleFromInternet.dates.get(0).getTime());
                        try {
                            substituteScheduleFromInternet.saveToFile(context.getExternalFilesDir(null));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        SubstituteScheduleDay todayFromInternet = substituteScheduleFromInternet.getDay(dateToday);

                        SubstituteScheduleDay todayFromFile = null;
                        try {
                            SubstituteSchedule substituteScheduleFromFile = SubstituteSchedule.loadFromFile(context.getExternalFilesDir(null));
                            todayFromFile = substituteScheduleFromFile.getDay(dateToday);
                        } catch (JSONException | ParseException | IOException e) {
                            e.printStackTrace();
                        }

                        //get SubjectSelection of first subject selection
                        File subjectSelectionDir = context.getExternalFilesDir(SubjectSelection.SUBJECT_SELECTION_DIR_NAME);
                        SubjectSelection selection = SubjectSelection.loadFromFile(subjectSelectionDir,
                                SubjectSelection.subjectSelectionNames(subjectSelectionDir).get(0));

                        TreeSet<String> entries = new TreeSet<>(todayFromInternet.getFilteredSubstituteScheduleEntries(selection));
                        TreeSet<String> entriesFromFile = null;
                        if (todayFromFile == null) {
                            entriesFromFile = new TreeSet<>();
                        } else {
                            entriesFromFile = new TreeSet<>(todayFromFile.getFilteredSubstituteScheduleEntries(selection));
                        }

                        entries.removeAll(entriesFromFile);

                        if (entries.size() > 0) {
                            Iterator<String> entriesIterator = entries.iterator();
                            String msg = "";
                            while (entriesIterator.hasNext()) {
                                msg += entriesIterator.next() + "\n";
                            }
                            String title = String.format("%1$ss Einträge von heute", selection.name);
                            sendNotification(context, title, String.format("%1$d neue Einträge", entries.size()), msg);
                        }

                    } catch (JSONException | ParseException | IOException | ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } finally {
                        if (looper != null) looper.quit();
                    }
                }
            };
            ParseRestClient.getSubstituteSchedule(context, responseHandler);
        }
    }

    private static void sendNotification(Context context, String title, String shortMsg, String msg) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher) //TODO: Use icon that gets displayed properly
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(shortMsg);

        mBuilder.setContentIntent(contentIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //My try
        /*
        int abc = 2;
        Intent broadcastIntent = new Intent(BROADCAST_ACTION)
                .putExtra(BROADCAST_DATA_STATUS, abc);
        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);
        */

        //From the sample scheduler app
        /*
        // If the app finds the string "doodle" in the Google home page content, it
        // indicates the presence of a doodle. Post a "Doodle Alert" notification.
        if (result.indexOf(SEARCH_STRING) != -1) {
            sendNotification(getString(R.string.doodle_found));
            Log.i(TAG, "Found doodle!!");
        } else {
            sendNotification(getString(R.string.no_doodle));
            Log.i(TAG, "No doodle found. :-(");
        }
        */

        Log.i(TAG, "NotificationService.onHandleIntent");

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        looper = Looper.myLooper();

        doYourJob(this);

        Looper.loop();

        looper = null;

        // Release the wake lock provided by the BroadcastReceiver.
        SubstituteScheduleAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }
}
