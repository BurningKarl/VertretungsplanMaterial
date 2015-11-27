package org.karlwelzel.vertretungsplan.material;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Karl on 19.11.2015.
 */
public class SubstituteScheduleNotificationService extends IntentService {

    public static final String BROADCAST_ACTION = "org.karlwelzel.vertretung-splantest.BROADCAST_ACTION";
    public static final String BROADCAST_DATA_STATUS = "org.karlwelzel.vertretungsplantest.BROADCAST_DATA_STATUS";

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
        int abc = 2;
        Intent broadcastIntent = new Intent(BROADCAST_ACTION)
                .putExtra(BROADCAST_DATA_STATUS, abc);
        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(broadcastIntent);

    }
}
