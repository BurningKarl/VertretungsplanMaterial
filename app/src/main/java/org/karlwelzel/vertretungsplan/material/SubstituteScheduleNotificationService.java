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

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    public static final String BROADCAST_ACTION = "org.karlwelzel.vertretung-splantest.BROADCAST_ACTION";
    public static final String BROADCAST_DATA_STATUS = "org.karlwelzel.vertretungsplantest.BROADCAST_DATA_STATUS";

    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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

        Log.i("NotificationService", "NotificationService.onHandle");

        if (isNetworkAvailable()) {
            JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("Notification", "onSuccess");
                    try {
                        SubstituteSchedule substituteScheduleFromInternet = new SubstituteSchedule(response.getString("json"));
                        try {
                            substituteScheduleFromInternet.saveToFile(getExternalFilesDir(null));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        SubstituteScheduleDay todayFromInternet = null;
                        Date date = null;
                        String date_string = "";
                        long nowMilliseconds = (new Date()).getTime();
/*
                            for (int i = 0; i < substituteScheduleFromFile.size(); i++) {
                                long timeDifference = nowMilliseconds - substituteScheduleFromFile.getDate(i).getTime();
                                if (0 < timeDifference && timeDifference < 24 * 60 * 60 * 1000) { //if first day is today
                                    todayFromFile = ((SubstituteScheduleDay) substituteScheduleFromFile.getDay(i));
                                }
                            }
*/
                        for (int i = 0; i < substituteScheduleFromInternet.size(); i++) {
                            date = substituteScheduleFromInternet.getDate(i);
                            long timeDifference = nowMilliseconds - date.getTime();
                            if (-24 * 60 * 60 * 1000 < timeDifference && timeDifference < 3 * 24 * 60 * 60 * 1000) {//&& timeDifference < 16 * 60 * 60 * 1000) {
                                if (timeDifference > 0) {
                                    date_string = getResources().getString(R.string.today);
                                } else if (-24 * 60 * 60 * 1000 < timeDifference) { //tomorrow
                                    date_string = getResources().getString(R.string.tomorrow);
                                }
                                date_string = date_string.toLowerCase();
                                todayFromInternet = substituteScheduleFromInternet.getDay(date);
                                break;
                            }
                        }

                        if (todayFromInternet == null) {
                            return;
                        }

                        //get SubjectSelection of first subject selection
                        File subjectSelectionDir = getExternalFilesDir(SubjectSelection.SUBJECT_SELECTION_DIR_NAME);
                        SubjectSelection selection = SubjectSelection.loadFromFile(subjectSelectionDir,
                                SubjectSelection.subjectSelectionNames(subjectSelectionDir).get(0));

                        SubstituteScheduleDay todayFromFile = null;
                        try {
                            SubstituteSchedule substituteScheduleFromFile = SubstituteSchedule.loadFromFile(getExternalFilesDir(null));
                            todayFromFile = substituteScheduleFromFile.getDay(date);
                        } catch (JSONException | ParseException | IOException e) {
                            e.printStackTrace();
                        }

                        if (todayFromFile == null) {
                            ArrayList<String> entriesFromToday = todayFromInternet.getFilteredSubstituteScheduleEntries(selection);

                            TreeSet<String> entries = (new TreeSet<>(entriesFromToday));

                            String title, msg = "";
                            if (entries.size() > 0) {
                                Iterator<String> entriesIterator = entries.iterator();
                                while (entriesIterator.hasNext()) {
                                    msg += entriesIterator.next() + "\n";
                                }
                                title = String.format("%1$ss Einträge von %2$s", selection.name, date_string);
                            } else {
                                title = String.format("%1$s hat %2$s keine Einträge", selection.name, date_string);
                            }

                            sendNotification(title, String.format("%1$d Einträge", entries.size()), msg);

                        } else {
                            ArrayList<String> entriesFromToday = todayFromInternet.getFilteredSubstituteScheduleEntries(selection);
                            ArrayList<String> entriesFromFile = todayFromFile == null ? new ArrayList<String>() : todayFromFile.getFilteredSubstituteScheduleEntries(selection);

                            TreeSet<String> entries = (new TreeSet<>(entriesFromToday));
                            entries.removeAll(entriesFromFile);

                            String title, msg = "";
                            if (entries.size() > 0) {
                                Iterator<String> entriesIterator = entries.iterator();
                                while (entriesIterator.hasNext()) {
                                    msg += entriesIterator.next() + "\n";
                                }
                                title = String.format("%1$ss Einträge von %2$s", selection.name, date_string);
                                sendNotification(title, String.format("%1$d neue Einträge", entries.size()), msg);
                            } else {
//                                title = String.format("%1$s hat %2$s keine Einträge", selection.name, date_string);
                            }
                        }

                    } catch (JSONException | ParseException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        Looper.myLooper().quit();
                    }
                }
            };
            ParseRestClient.getSubstituteSchedule(this, responseHandler);
        }

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Looper.loop();

        // Release the wake lock provided by the BroadcastReceiver.
        SubstituteScheduleAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)

    }

    private void sendNotification(String title, String shortMsg, String msg) {
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
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
}
