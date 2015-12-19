package org.karlwelzel.vertretungsplan.material;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This BroadcastReceiver automatically (re)starts the alarm when the device is
 * rebooted. This receiver is set to be disabled (android:enabled="false") in the
 * application's manifest file. When the user sets the alarm, the receiver is enabled.
 * When the user cancels the alarm, the receiver is disabled, so that rebooting the
 * device will not trigger this receiver.
 */
// BEGIN_INCLUDE(autostart)
public class SubstituteScheduleBroadcastReceiver extends BroadcastReceiver {
    SubstituteScheduleAlarmReceiver alarm = new SubstituteScheduleAlarmReceiver();

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendNotification(Context context, String title, String shortMsg, String msg) {
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
        notificationManager.notify(1, notification);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BroadcastReceiver", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            //TODO: Remove this notification
            sendNotification(context, "Boot Receiver", "short message", "long message");
            alarm.setAlarm(context);
            SubstituteScheduleNotificationService.doYourJob(context);
        } else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") && isNetworkAvailable(context)){
            SubstituteScheduleNotificationService.doYourJob(context);
        }
    }
}
//END_INCLUDE(autostart)
